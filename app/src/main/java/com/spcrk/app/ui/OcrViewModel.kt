package com.spcrk.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.ai.api.OcrTextBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OcrUiState(
    val recognizedText: String = "",
    val textBlocks: List<OcrTextBlock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showChinese: Boolean = false,
    val selectedImageBitmap: Bitmap? = null,
    val isRecognizing: Boolean = false
)

class OcrViewModel(application: Application) : AndroidViewModel(application) {

    private val container: AppContainer = getAppContainer(application)
    private val ocrService = container.ocrService

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun setSelectedImageBitmap(bitmap: Bitmap?) {
        _uiState.value = _uiState.value.copy(selectedImageBitmap = bitmap)
    }

    fun setRecognizeChinese(recognizeChinese: Boolean) {
        _uiState.value = _uiState.value.copy(showChinese = recognizeChinese)
    }

    suspend fun recognizeImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(isRecognizing = true)
        try {
            val text = ocrService.recognizeText(uri, _uiState.value.showChinese)
            _uiState.value = _uiState.value.copy(recognizedText = text, isRecognizing = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message, isRecognizing = false)
        }
    }

    fun toggleChinese() {
        _uiState.value = _uiState.value.copy(showChinese = !_uiState.value.showChinese)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(recognizedText = "", textBlocks = emptyList())
    }

    fun getErrorMessage(): String? = _uiState.value.errorMessage
}

class OcrViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OcrViewModel::class.java))
            return OcrViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
