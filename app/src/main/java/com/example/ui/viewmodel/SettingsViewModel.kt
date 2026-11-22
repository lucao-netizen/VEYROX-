package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ChatRepository
import com.example.domain.model.AiEngineType
import com.example.domain.model.AiMode
import com.example.domain.model.ThemePreference
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BackendCheckStatus {
    data object Idle : BackendCheckStatus
    data object Checking : BackendCheckStatus
    data class Healthy(val message: String) : BackendCheckStatus
    data class Failed(val message: String) : BackendCheckStatus
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val conversationCount: Int = 0,
    val backendCheckStatus: BackendCheckStatus = BackendCheckStatus.Idle,
    val statusBanner: String? = null
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _backendStatus = MutableStateFlow<BackendCheckStatus>(BackendCheckStatus.Idle)
    private val _statusBanner = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.preferences,
        chatRepository.observeAllConversations(),
        _backendStatus,
        _statusBanner
    ) { prefs, conversations, backendStatus, banner ->
        SettingsUiState(
            preferences = prefs,
            conversationCount = conversations.size,
            backendCheckStatus = backendStatus,
            statusBanner = banner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState()
    )

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            preferencesRepository.setThemePreference(theme)
        }
    }

    fun setAiEngineType(engine: AiEngineType) {
        viewModelScope.launch {
            preferencesRepository.setAiEngineType(engine)
        }
    }

    fun setStreamingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setStreamingEnabled(enabled)
        }
    }

    fun setDefaultAiMode(mode: AiMode) {
        viewModelScope.launch {
            preferencesRepository.setDefaultAiMode(mode)
        }
    }

    fun saveCustomBackendUrl(url: String) {
        viewModelScope.launch {
            preferencesRepository.setCustomBackendUrl(url)
            _backendStatus.value = BackendCheckStatus.Idle
        }
    }

    fun testBackendHealth(url: String) {
        viewModelScope.launch {
            _backendStatus.value = BackendCheckStatus.Checking
            val result = chatRepository.checkCustomBackendHealth(url)
            _backendStatus.value = result.fold(
                onSuccess = { health ->
                    val modeTag = if (health.demoMode) "Demo Mode" else "Live"
                    BackendCheckStatus.Healthy("Connected: ${health.status} ($modeTag)")
                },
                onFailure = { error ->
                    BackendCheckStatus.Failed(
                        error.message ?: "Unable to connect to backend."
                    )
                }
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatRepository.deleteAllConversations()
            _statusBanner.value = "All local conversations cleared."
        }
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    class Factory(
        private val preferencesRepository: UserPreferencesRepository,
        private val chatRepository: ChatRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesRepository, chatRepository) as T
        }
    }
}
