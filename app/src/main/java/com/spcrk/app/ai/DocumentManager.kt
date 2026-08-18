package com.spcrk.app.ai

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.spcrk.app.data.KnowledgeDocument
import com.spcrk.app.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class DocumentInfo(
    val uri: String,
    val name: String,
    val size: Long,
    val content: String,
    val uploadedAt: Long = System.currentTimeMillis()
)

class DocumentManager(private val context: Context) {
    companion object {
        const val MAX_FILE_SIZE = 10 * 1024 * 1024
    }

    private val documents = mutableListOf<DocumentInfo>()
    private val repository = Repository(context)

    suspend fun loadDocument(uri: Uri): DocumentInfo = withContext(Dispatchers.IO) {
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

        if (size > MAX_FILE_SIZE) {
            throw IllegalStateException("文件大小超过限制（最大 10MB）")
        }

        val content = when {
            name.endsWith(".pdf") -> readPdf(uri)
            name.endsWith(".docx") -> readDocx(uri)
            name.endsWith(".doc") -> readDoc(uri)
            name.endsWith(".md") -> readText(uri)
            name.endsWith(".txt") -> readText(uri)
            name.endsWith(".kt") -> readText(uri)
            name.endsWith(".java") -> readText(uri)
            name.endsWith(".py") -> readText(uri)
            name.endsWith(".js") -> readText(uri)
            name.endsWith(".json") -> readText(uri)
            name.endsWith(".xml") -> readText(uri)
            name.endsWith(".html") -> readText(uri)
            name.endsWith(".css") -> readText(uri)
            else -> readText(uri)
        }

        val doc = DocumentInfo(uri.toString(), name, size, content)
        documents.add(doc)
        return@withContext doc
    }

    suspend fun saveToKnowledgeBase(document: DocumentInfo): Long = withContext(Dispatchers.IO) {
        val fileType = when {
            document.name.endsWith(".pdf") -> "pdf"
            document.name.endsWith(".docx") -> "docx"
            document.name.endsWith(".doc") -> "doc"
            document.name.endsWith(".md") -> "md"
            document.name.endsWith(".txt") -> "txt"
            document.name.endsWith(".kt") -> "kt"
            document.name.endsWith(".java") -> "java"
            document.name.endsWith(".py") -> "py"
            document.name.endsWith(".js") -> "js"
            document.name.endsWith(".json") -> "json"
            document.name.endsWith(".xml") -> "xml"
            document.name.endsWith(".html") -> "html"
            document.name.endsWith(".css") -> "css"
            else -> "unknown"
        }

        val chunks = splitIntoChunks(document.content, 1000)
        val knowledgeDoc = KnowledgeDocument(
            title = document.name,
            content = document.content,
            filePath = document.uri,
            fileType = fileType,
            chunkCount = chunks.size
        )
        return@withContext repository.addKnowledgeDocument(knowledgeDoc)
    }

    fun getKnowledgeDocuments() = repository.getAllKnowledgeDocuments()

    fun searchKnowledgeDocuments(query: String) = repository.searchKnowledgeDocuments(query)

    suspend fun removeKnowledgeDocument(id: Long) = withContext(Dispatchers.IO) {
        repository.getKnowledgeDocumentById(id)?.let {
            repository.deleteKnowledgeDocument(it)
        }
    }

    suspend fun clearKnowledgeBase() = withContext(Dispatchers.IO) {
        val docs = repository.getAllKnowledgeDocuments()
        docs.collect { list ->
            list.forEach { doc ->
                repository.deleteKnowledgeDocument(doc)
            }
        }
    }

    private fun splitIntoChunks(content: String, chunkSize: Int): List<String> {
        if (content.length <= chunkSize) return listOf(content)
        val chunks = mutableListOf<String>()
        var startIndex = 0
        while (startIndex < content.length) {
            val endIndex = minOf(startIndex + chunkSize, content.length)
            chunks.add(content.substring(startIndex, endIndex))
            startIndex = endIndex
        }
        return chunks
    }

    private suspend fun readText(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    return@withContext reader.readText()
                }
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun readPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = org.apache.pdfbox.pdmodel.PDDocument.load(inputStream)
                val content = try {
                    org.apache.pdfbox.text.PDFTextStripper().getText(document)
                } finally {
                    document.close()
                }
                return@withContext content
            } ?: ""
        } catch (e: Exception) {
            "PDF 解析失败: ${e.message}"
        }
    }

    private suspend fun readDocx(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = org.apache.poi.xwpf.usermodel.XWPFDocument(inputStream)
                val content = StringBuilder()
                document.paragraphs.forEach { paragraph ->
                    content.append(paragraph.text)
                    content.append("\n")
                }
                document.close()
                return@withContext content.toString()
            } ?: ""
        } catch (e: Exception) {
            "Word 文档解析失败: ${e.message}"
        }
    }

    private suspend fun readDoc(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = org.apache.poi.hwpf.HWPFDocument(inputStream)
                val content = document.documentText
                document.close()
                return@withContext content
            } ?: ""
        } catch (e: Exception) {
            "Word 文档解析失败: ${e.message}"
        }
    }

    fun getDocuments(): List<DocumentInfo> = documents.toList()

    fun removeDocument(uri: String) {
        documents.removeAll { it.uri == uri }
    }

    fun clearDocuments() {
        documents.clear()
    }
}
