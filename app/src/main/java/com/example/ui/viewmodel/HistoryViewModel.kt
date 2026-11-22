package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ConversationEntity
import com.example.data.repository.ChatRepository
import com.example.domain.model.AiMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val conversations: List<ConversationEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedModeFilter: AiMode? = null,
    val totalCount: Int = 0
)

class HistoryViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _modeFilter = MutableStateFlow<AiMode?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        chatRepository.observeAllConversations(),
        _searchQuery,
        _modeFilter
    ) { allConversations, query, modeFilter ->
        val trimmed = query.trim()
        val filtered = allConversations.filter { conv ->
            val matchesMode = modeFilter == null || conv.aiMode == modeFilter
            val matchesQuery = trimmed.isEmpty() ||
                conv.title.contains(trimmed, ignoreCase = true) ||
                conv.lastMessagePreview.contains(trimmed, ignoreCase = true)
            matchesMode && matchesQuery
        }
        HistoryUiState(
            conversations = filtered,
            searchQuery = query,
            selectedModeFilter = modeFilter,
            totalCount = allConversations.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HistoryUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectModeFilter(mode: AiMode?) {
        _modeFilter.value = if (_modeFilter.value == mode) null else mode
    }

    fun renameConversation(conversationId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.renameConversation(conversationId, newTitle)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)
        }
    }

    fun createNewConversation(mode: AiMode = AiMode.GENERAL, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val id = chatRepository.createConversation(mode = mode)
            onCreated(id)
        }
    }

    class Factory(
        private val chatRepository: ChatRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(chatRepository) as T
        }
    }
}
