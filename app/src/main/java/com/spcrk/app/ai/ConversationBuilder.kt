package com.spcrk.app.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.spcrk.app.data.Repository
import com.spcrk.app.ai.SearchResult
import com.spcrk.app.ai.ChatUiState
import kotlinx.coroutines.flow.firstOrNull
import java.io.ByteArrayOutputStream
import java.util.Base64

class ConversationBuilder(
    private val getUiState: () -> ChatUiState,
    private val repository: Repository,
    private val documentService: com.spcrk.app.ai.api.DocumentService,
    private val ocrService: com.spcrk.app.ai.api.OcrService,
    private val appProvider: () -> android.app.Application
) {

    private val visionModels = setOf(
        "gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4-vision", "gpt-4v",
        "claude-3", "claude-3-5", "claude-3-7", "claude-sonnet", "claude-opus",
        "gemini-1.5", "gemini-2", "gemini-pro-vision", "gemini-3", "gemini-3.1", "gemini-3.5", "gemini-3.6",
        "agnes-2.5-flash", "agnes-2.0-flash",
        "doubao-seed-1.6-vision", "qwen-vl", "qwen2-vl",
        "llava", "molmo", "internvl", "phi-3-vision"
    )

    suspend fun buildConversationMessages(
        currentMessage: Message,
        searchResults: List<SearchResult>
    ): List<Map<String, Any>> {
        val messages = mutableListOf<Map<String, Any>>()

        try {
            val history = repository.getConversation(getUiState().currentConversationId).firstOrNull() ?: emptyList()
            history.forEach { dbMsg ->
                messages.add(mapOf("role" to dbMsg.role, "content" to dbMsg.content))
            }
        } catch (e: Exception) {
            println("[ConversationBuilder] load history failed: ${e.message}")
        }

        if (searchResults.isNotEmpty()) {
            val contextBuilder = StringBuilder()
            contextBuilder.appendLine("以下是与用户问题相关的网络搜索结果：")
            searchResults.forEachIndexed { index, result ->
                contextBuilder.appendLine("${index + 1}. ${result.title}")
                contextBuilder.appendLine("   URL: ${result.url}")
                contextBuilder.appendLine("   摘要: ${result.snippet}")
            }
            contextBuilder.appendLine("\n请基于以上搜索结果回答用户的问题。")
            messages.add(mapOf("role" to "system", "content" to contextBuilder.toString()))
        }

        if (getUiState().enableKnowledge) {
            val knowledgeContext = queryKnowledgeBase(currentMessage.content)
            if (knowledgeContext != null) {
                messages.add(mapOf("role" to "system", "content" to knowledgeContext))
            }
        }

        val attachedDoc = getUiState().attachedDocument
        if (attachedDoc != null) {
            try {
                val uri = Uri.parse(attachedDoc)
                val docInfo = documentService.loadDocument(uri)
                val docContext = StringBuilder()
                docContext.appendLine("用户附加了文档：${docInfo.name}")
                docContext.appendLine("文档内容：")
                docContext.appendLine(docInfo.content)
                messages.add(mapOf("role" to "system", "content" to docContext.toString()))
            } catch (e: Exception) {
                println("[ConversationBuilder] load document failed: ${e.message}")
            }
        }

        val attachedImg = getUiState().attachedImageUri
        if (attachedImg != null) {
            try {
                val uri = Uri.parse(attachedImg)
                if (modelSupportsVision(getUiState().currentModel)) {
                    val base64 = imageUriToBase64(uri)
                    if (base64.isNotEmpty()) {
                        messages.add(
                            mapOf(
                                "role" to "user",
                                "content" to listOf(
                                    mapOf("type" to "text", "text" to "用户附加了图片"),
                                    mapOf(
                                        "type" to "image_url",
                                        "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64")
                                    )
                                )
                            )
                        )
                    }
                } else {
                    val ocrText = ocrService.recognizeText(uri, false)
                    if (ocrText.isNotEmpty()) {
                        val imgContext = StringBuilder()
                        imgContext.appendLine("用户附加了图片，OCR 识别结果：")
                        imgContext.appendLine(ocrText)
                        messages.add(mapOf("role" to "system", "content" to imgContext.toString()))
                    }
                }
            } catch (e: Exception) {
                println("[ConversationBuilder] process image failed: ${e.message}")
            }
        }

        messages.add(mapOf("role" to "user", "content" to currentMessage.content))

        return messages
    }

    private suspend fun queryKnowledgeBase(query: String): String? {
        return try {
            val docs = repository.searchKnowledgeDocuments(query).firstOrNull() ?: return null
            if (docs.isEmpty()) return null
            val sb = StringBuilder()
            sb.appendLine("以下是知识库中相关的参考资料：")
            docs.take(5).forEach { doc ->
                sb.appendLine("【${doc.title}】")
                sb.appendLine(doc.content.take(500))
                sb.appendLine()
            }
            sb.toString()
        } catch (e: Exception) {
            println("[ConversationBuilder] query knowledge base failed: ${e.message}")
            null
        }
    }

    private fun imageUriToBase64(uri: Uri): String {
        return try {
            val inputStream = appProvider().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return ""
            val resized = if (bitmap.width > 1024 || bitmap.height > 1024) {
                val ratio = minOf(1024f / bitmap.width, 1024f / bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else bitmap
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bytes = baos.toByteArray()
            if (resized != bitmap) resized.recycle()
            bitmap.recycle()
            Base64.getEncoder().encodeToString(bytes)
        } catch (e: Exception) {
            println("[ConversationBuilder] image to base64 failed: ${e.message}")
            ""
        }
    }

    private fun modelSupportsVision(modelName: String): Boolean {
        val lower = modelName.lowercase()
        return visionModels.any { lower.contains(it) }
    }
}
