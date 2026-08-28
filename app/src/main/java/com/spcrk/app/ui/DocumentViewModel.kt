package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.ai.api.DocumentService
import com.spcrk.app.data.KnowledgeDocument
import com.spcrk.app.data.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

data class DocumentUiState(
    val documents: List<KnowledgeDocument> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val container: AppContainer = getAppContainer(application)
    private val repository = container.repository
    private val documentService = container.documentService

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    init { loadDocuments() }

    private fun loadDocuments() {
        viewModelScope.launch {
            repository.getAllKnowledgeDocuments().collect { documents ->
                _uiState.value = DocumentUiState(documents = documents, isLoading = false)
            }
        }
    }

    fun getDocuments(): Flow<List<KnowledgeDocument>> = repository.getAllKnowledgeDocuments()
    fun getErrorMessage(): String? = _uiState.value.errorMessage

    fun loadDocument(uri: String) {
        viewModelScope.launch {
            try {
                val docInfo = documentService.loadDocument(android.net.Uri.parse(uri))
                val id = documentService.saveToKnowledgeBase(docInfo)
                if (id > 0) loadDocuments()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    fun removeDocument(uri: String) {
        viewModelScope.launch {
            val docs = repository.getAllKnowledgeDocuments().firstOrNull() ?: emptyList()
            docs.find { it.filePath == uri }?.let { repository.deleteKnowledgeDocument(it) }
            loadDocuments()
        }
    }
}

class DocumentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java))
            return DocumentViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
