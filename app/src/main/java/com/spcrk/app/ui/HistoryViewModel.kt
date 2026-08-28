package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.DownloadHistory
import com.spcrk.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class HistoryUiState(
    val histories: List<DownloadHistory> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val deleteTarget: DownloadHistory? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val container: AppContainer = getAppContainer(application)
    private val repository = container.repository
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllHistory().collect { histories ->
                _uiState.value = _uiState.value.copy(
                    histories = histories,
                    isLoading = false
                )
            }
        }
    }

    fun showDeleteDialog(history: DownloadHistory) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true, deleteTarget = history)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false, deleteTarget = null)
    }

    fun deleteHistory(deleteFile: Boolean = false) {
        val history = _uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            if (deleteFile) {
                try {
                    if (com.spcrk.app.downloader.PlaybackUri.isContentUri(history.filePath)) {
                        // Android 10+ MediaStore 檔案需透過 ContentResolver 刪除
                        getApplication<Application>().contentResolver
                            .delete(android.net.Uri.parse(history.filePath), null, null)
                    } else {
                        File(history.filePath).delete()
                    }
                } catch (e: Exception) {
                    println("[HistoryViewModel] delete file failed: ${e.message}")
                }
            }
            repository.deleteHistory(history)
            _uiState.value = _uiState.value.copy(showDeleteDialog = false, deleteTarget = null)
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes.toFloat() / (1024 * 1024))
        else -> String.format("%.1f GB", bytes.toFloat() / (1024 * 1024 * 1024))
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
