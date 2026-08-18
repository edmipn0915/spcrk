package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.data.Note
import com.spcrk.app.VideoDownloaderApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)

data class NoteEditUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false
)

class NoteListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as VideoDownloaderApp).repository
    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                _uiState.value = NoteListUiState(notes = notes, isLoading = false)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }
}

class NoteEditViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as VideoDownloaderApp).repository
    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            if (note != null) {
                _uiState.value = NoteEditUiState(noteId = note.id, title = note.title, content = note.content)
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return
        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (state.noteId != null) {
                repository.updateNote(Note(id = state.noteId, title = state.title, content = state.content, createdAt = now, updatedAt = now))
            } else {
                repository.addNote(Note(title = state.title, content = state.content, createdAt = now, updatedAt = now))
            }
            _uiState.value = state.copy(isSaving = false)
        }
    }
}
