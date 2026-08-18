package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.data.ChatMessage
import com.spcrk.app.data.DownloadHistory
import com.spcrk.app.data.Note
import com.spcrk.app.VideoDownloaderApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchResult(
    val type: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val id: Long = 0,
    val conversationId: String = ""
)

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedTab: Int = 0
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as VideoDownloaderApp).repository
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allHistories: List<DownloadHistory> = emptyList()
    private var allNotes: List<Note> = emptyList()
    private var allConversations: Map<String, List<ChatMessage>> = emptyMap()

    init {
        viewModelScope.launch {
            repository.getAllHistory().collect { allHistories = it }
        }
        viewModelScope.launch {
            repository.getAllNotes().collect { allNotes = it }
        }
        viewModelScope.launch {
            repository.getConversations().collect { conversationIds ->
                val convMap = mutableMapOf<String, List<ChatMessage>>()
                conversationIds.forEach { convId ->
                    repository.getConversation(convId).collect { messages ->
                        convMap[convId] = messages
                    }
                }
                allConversations = convMap
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        search()
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
        search()
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
            return
        }
        _uiState.value = _uiState.value.copy(isSearching = true)

        val tab = _uiState.value.selectedTab
        val results = mutableListOf<SearchResult>()

        if (tab == 0 || tab == 1) {
            allHistories.filter { it.title.contains(query, ignoreCase = true) }.forEach {
                results.add(SearchResult("history", it.title, it.platform, it.downloadTime, it.id))
            }
        }
        if (tab == 0 || tab == 2) {
            allNotes.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }.forEach {
                results.add(SearchResult("note", it.title, it.content.take(100), it.updatedAt, it.id))
            }
        }
        if (tab == 0 || tab == 3) {
            allConversations.forEach { (convId, messages) ->
                val matchingMessage = messages.firstOrNull {
                    it.content.contains(query, ignoreCase = true)
                }
                if (matchingMessage != null) {
                    results.add(
                        SearchResult(
                            type = "chat",
                            title = convId,
                            content = matchingMessage.content.take(100),
                            timestamp = matchingMessage.timestamp,
                            conversationId = convId
                        )
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(results = results, isSearching = false)
    }
}
