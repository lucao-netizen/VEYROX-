package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ConversationEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ChatRepository
import com.example.domain.model.AiMode
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentConversations: List<ConversationEntity> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val chatRepository: ChatRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        chatRepository.observeRecentConversations(limit = 6),
        preferencesRepository.preferences
    ) { recent, prefs ->
        HomeUiState(
            recentConversations = recent,
            preferences = prefs,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState()
    )

    fun startNewConversation(mode: AiMode, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val id = chatRepository.createConversation(mode = mode)
            onCreated(id)
        }
    }

    class Factory(
        private val chatRepository: ChatRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(chatRepository, preferencesRepository) as T
        }
    }
}
