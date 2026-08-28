package com.spcrk.app.ai

import com.spcrk.app.ai.api.DocumentInfo
import com.spcrk.app.ai.api.DocumentService
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.spcrk.app.data.KnowledgeDocument
import com.spcrk.app.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentManager(
    private val context: Context,
    private val repository: Repository = Repository(context)
) : DocumentService {
    companion object {
        const val MAX_FILE_SIZE = 10 * 1024 * 1024
    }

    override suspend fun loadDocument(uri: Uri): DocumentInfo = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        var name = "unknown"
        var size = 0L
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = it.getString(nameIndex) ?: "unknown"
                if (sizeIndex >= 0) size = it.getLong(sizeIndex)
            }
        }
        if (size > MAX_FILE_SIZE) throw IllegalStateException("文件大小超过限制（最大 10MB）")
        val content = when {
            name.endsWith(".pdf")      -> readPdf(uri)
            name.endsWith(".docx")     -> readDocx(uri)
            name.endsWith(".doc")      -> readDoc(uri)
            else                       -> readText(uri)
        }
        DocumentInfo(uri.toString(), name, size, content)
    }

    override suspend fun saveToKnowledgeBase(document: DocumentInfo): Long = withContext(Dispatchers.IO) {
        val fileType = when {
            document.name.endsWith(".pdf") -> "pdf"
            document.name.endsWith(".docx") -> "docx"
            document.name.endsWith(".doc")  -> "doc"
            document.name.endsWith(".md")   -> "md"
            document.name.endsWith(".txt")  -> "txt"
            else -> "unknown"
        }
        val chunks = splitIntoChunks(document.content, 1000)
        val knowledgeDoc = KnowledgeDocument(
            title = document.name, content = document.content,
            filePath = document.uri, fileType = fileType,
            chunkCount = chunks.size
        )
        repository.addKnowledgeDocument(knowledgeDoc)
    }

    override fun getKnowledgeDocuments() = repository.getAllKnowledgeDocuments()
    override fun searchKnowledgeDocuments(query: String) = repository.searchKnowledgeDocuments(query)
    override suspend fun removeKnowledgeDocument(id: Long) {
        withContext(Dispatchers.IO) {
            repository.getKnowledgeDocumentById(id)?.let { repository.deleteKnowledgeDocument(it) }
        }
    }
    override suspend fun clearKnowledgeBase() = withContext(Dispatchers.IO) {
        repository.getAllKnowledgeDocuments().collect { list ->
            list.forEach { repository.deleteKnowledgeDocument(it) }
        }
    }

    private fun splitIntoChunks(content: String, chunkSize: Int): List<String> {
        if (content.length <= chunkSize) return listOf(content)
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < content.length) {
            chunks.add(content.substring(start, start.coerceAtMost(start + chunkSize)))
            start += chunkSize
        }
        return chunks
    }

    private suspend fun readText(uri: Uri): String = withContext(Dispatchers.IO) {
        try { context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() } ?: "" }
        catch (e: Exception) { "" }
    }

    private suspend fun readPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val document = org.apache.pdfbox.pdmodel.PDDocument.load(stream)
                val content = org.apache.pdfbox.text.PDFTextStripper().getText(document)
                document.close()
                content
            } ?: ""
        } catch (e: Exception) { "PDF 解析失败: ${e.message}" }
    }

    private suspend fun readDocx(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val document = org.apache.poi.xwpf.usermodel.XWPFDocument(stream)
                val content = StringBuilder().also { sb ->
                    document.paragraphs.forEach { sb.append(it.text).append("\n") }
                }.toString()
                document.close()
                content
            } ?: ""
        } catch (e: Exception) { "Word 文档解析失败: ${e.message}" }
    }

    private suspend fun readDoc(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = org.apache.poi.hwpf.HWPFDocument(stream)
                val content = doc.documentText
                doc.close()
                content
            } ?: ""
        } catch (e: Exception) { "Word 文档解析失败: ${e.message}" }
    }
}
