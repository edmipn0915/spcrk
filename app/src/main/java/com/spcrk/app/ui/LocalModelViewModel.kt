package com.spcrk.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.ai.api.EmbeddingModelInfo
import com.spcrk.app.ai.api.LocalModelInfo
import com.spcrk.app.ai.api.LocalModelService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocalModelUiState(
    val embeddingModels: List<EmbeddingModelInfo> = emptyList(),
    val llmModels: List<EmbeddingModelInfo> = emptyList(),
    val downloadedModels: List<LocalModelInfo> = emptyList(),
    val downloadingIds: Set<String> = emptySet(),
    val downloadProgress: Map<String, Float> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class LocalModelViewModel(application: Application) : AndroidViewModel(application) {

    private val container: AppContainer = getAppContainer(application)
    private val localModelService = container.localModelService

    private val _uiState = MutableStateFlow(LocalModelUiState())
    val uiState: StateFlow<LocalModelUiState> = _uiState.asStateFlow()

    init { loadModels() }

    private fun loadModels() {
        viewModelScope.launch {
            _uiState.value = LocalModelUiState(
                embeddingModels = localModelService.getAvailableEmbeddingModels(),
                llmModels = localModelService.getAvailableLLMModels(),
                downloadedModels = localModelService.getDownloadedModels(),
                isLoading = false
            )
        }
    }

    fun refreshModels() { loadModels() }

    fun importLocalFile(uri: Uri): Boolean {
        val success = localModelService.importLocalFile(uri)
        if (success) loadModels()
        return success
    }

    fun isModelDownloaded(fileName: String): Boolean = localModelService.isModelDownloaded(fileName)

    fun downloadModel(url: String, fileName: String, onProgress: (String, Float) -> Unit, onComplete: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            localModelService.downloadModel(
                url = url,
                fileName = fileName,
                onProgress = { p -> onProgress(fileName, p) },
                onComplete = { success ->
                    onComplete(fileName, success)
                    if (success) loadModels()
                }
            )
        }
    }

    fun deleteModel(fileName: String): Boolean {
        val success = localModelService.deleteModel(fileName)
        if (success) loadModels()
        return success
    }

    fun getErrorMessage(): String? = _uiState.value.errorMessage
}

class LocalModelViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocalModelViewModel::class.java))
            return LocalModelViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
