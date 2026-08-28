package com.spcrk.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.KnowledgeCategory
import com.spcrk.app.data.KnowledgeDocument
import com.spcrk.app.data.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KnowledgeUiState(
    val categories: List<KnowledgeCategory> = emptyList(),
    val documents: List<KnowledgeDocument> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val showAddCategoryDialog: Boolean = false,
    val showCategorySelector: Boolean = false,
    val selectedCategoryId: Long? = null,
    val uploadCategoryId: Long? = null
)

class KnowledgeViewModel(application: Application) : AndroidViewModel(application) {

    private val container: AppContainer = getAppContainer(application)
    private val repository = container.repository
    private val documentService = container.documentService

    private val _uiState = MutableStateFlow(KnowledgeUiState())
    val uiState: StateFlow<KnowledgeUiState> = _uiState.asStateFlow()

    init {
        refreshCategories()
        refreshDocuments()
    }

    fun refreshCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun refreshDocuments() {
        viewModelScope.launch {
            getDocumentsFlow().collect { documents ->
                _uiState.value = _uiState.value.copy(documents = documents)
            }
        }
    }

    private fun getDocumentsFlow(): Flow<List<KnowledgeDocument>> {
        return when {
            _uiState.value.isSearching && _uiState.value.searchQuery.isNotEmpty() ->
                repository.searchKnowledgeDocuments(_uiState.value.searchQuery)
            _uiState.value.selectedCategoryId != null ->
                repository.getKnowledgeDocumentsByCategory(_uiState.value.selectedCategoryId!!)
            else -> repository.getAllKnowledgeDocuments()
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshDocuments()
    }

    fun toggleSearch(isSearching: Boolean) {
        _uiState.value = _uiState.value.copy(isSearching = isSearching)
        if (!isSearching) _uiState.value = _uiState.value.copy(searchQuery = "")
        refreshDocuments()
    }

    fun setSelectedCategoryId(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        refreshDocuments()
    }

    fun showAddCategoryDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showAddCategoryDialog = show) }
    fun setShowCategorySelector(show: Boolean) { _uiState.value = _uiState.value.copy(showCategorySelector = show) }
    fun setUploadCategoryId(categoryId: Long?) { _uiState.value = _uiState.value.copy(uploadCategoryId = categoryId) }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.addCategory(KnowledgeCategory(name = name, type = type))
            refreshCategories()
        }
    }

    fun deleteCategory(category: KnowledgeCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            if (_uiState.value.selectedCategoryId == category.id) setSelectedCategoryId(null)
            refreshCategories()
        }
    }

    fun deleteDocument(document: KnowledgeDocument) {
        viewModelScope.launch {
            repository.deleteKnowledgeDocument(document)
            refreshDocuments()
        }
    }

    fun uploadDocument(uri: Uri) {
        viewModelScope.launch {
            try {
                val docInfo = documentService.loadDocument(uri)
                repository.addKnowledgeDocument(
                    KnowledgeDocument(
                        title = docInfo.name, content = docInfo.content,
                        filePath = docInfo.uri,
                        fileType = docInfo.name.substringAfterLast('.', "unknown"),
                        chunkCount = 1,
                        categoryId = _uiState.value.uploadCategoryId
                    )
                )
                setUploadCategoryId(null)
                refreshDocuments()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "文件上传失败")
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun getCategoryById(id: Long?): KnowledgeCategory? = _uiState.value.categories.find { it.id == id }
}

class KnowledgeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KnowledgeViewModel::class.java))
            return KnowledgeViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
