package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiStreamEvent
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ChatRepository
import com.example.domain.model.AiMode
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversation: ConversationEntity? = null,
    val messages: List<MessageEntity> = emptyList(),
    val activeMode: AiMode = AiMode.GENERAL,
    val preferences: UserPreferences = UserPreferences(),
    val streamingContent: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val lastFailedPrompt: String? = null
)

class ChatViewModel(
    private val conversationId: String,
    initialMode: AiMode,
    private val initialPrompt: String?,
    private val chatRepository: ChatRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _ephemeralState = MutableStateFlow(
        EphemeralChatState(activeMode = initialMode)
    )
    private var generationJob: Job? = null

    private data class EphemeralChatState(
        val activeMode: AiMode,
        val streamingContent: String = "",
        val isGenerating: Boolean = false,
        val errorMessage: String? = null,
        val lastFailedPrompt: String? = null
    )

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepository.observeConversation(conversationId),
        chatRepository.observeMessages(conversationId),
        preferencesRepository.preferences,
        _ephemeralState
    ) { conversation, messages, prefs, ephemeral ->
        ChatUiState(
            conversation = conversation,
            messages = messages,
            activeMode = conversation?.aiMode ?: ephemeral.activeMode,
            preferences = prefs,
            streamingContent = ephemeral.streamingContent,
            isGenerating = ephemeral.isGenerating,
            errorMessage = ephemeral.errorMessage,
            lastFailedPrompt = ephemeral.lastFailedPrompt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ChatUiState(activeMode = initialMode)
    )

    init {
        if (!initialPrompt.isNullOrBlank()) {
            sendMessage(initialPrompt)
        }
    }

    fun switchMode(mode: AiMode) {
        _ephemeralState.update { it.copy(activeMode = mode) }
        viewModelScope.launch {
            chatRepository.updateConversationMode(conversationId, mode)
        }
    }

    fun dismissError() {
        _ephemeralState.update { it.copy(errorMessage = null) }
    }

    fun sendMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty()) {
            _ephemeralState.update {
                it.copy(errorMessage = "Please enter a message before sending.")
            }
            return
        }
        if (_ephemeralState.value.isGenerating) return

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            val mode = uiState.value.activeMode
            val historyBeforeSend = chatRepository.getHistoryTurns(conversationId)

            chatRepository.saveUserMessage(
                conversationId = conversationId,
                content = text,
                mode = mode
            )

            _ephemeralState.update {
                it.copy(
                    isGenerating = true,
                    streamingContent = "",
                    errorMessage = null,
                    lastFailedPrompt = null
                )
            }

            val buffer = StringBuilder()
            chatRepository.streamAiReply(
                engineType = prefs.aiEngineType,
                prompt = text,
                history = historyBeforeSend,
                mode = mode,
                streaming = prefs.streamingEnabled,
                customBackendUrl = prefs.customBackendUrl
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Chunk -> {
                        buffer.append(event.delta)
                        _ephemeralState.update {
                            it.copy(streamingContent = buffer.toString())
                        }
                    }

                    is AiStreamEvent.Completed -> {
                        chatRepository.saveAssistantMessage(
                            conversationId = conversationId,
                            content = event.fullText,
                            isDemoResponse = event.isDemo,
                            isError = false
                        )
                        _ephemeralState.update {
                            it.copy(
                                isGenerating = false,
                                streamingContent = "",
                                errorMessage = null,
                                lastFailedPrompt = null
                            )
                        }
                    }

                    is AiStreamEvent.Error -> {
                        _ephemeralState.update {
                            it.copy(
                                isGenerating = false,
                                streamingContent = "",
                                errorMessage = event.error.userMessage,
                                lastFailedPrompt = text
                            )
                        }
                    }
                }
            }
        }
    }

    fun retryLastPrompt() {
        val failed = _ephemeralState.value.lastFailedPrompt ?: return
        sendMessage(failed)
    }

    fun stopGeneration() {
        generationJob?.cancel()
        val partial = _ephemeralState.value.streamingContent.trim()
        viewModelScope.launch {
            if (partial.isNotEmpty()) {
                val isDemo = uiState.value.preferences.aiEngineType.name == "DEMO_MODE"
                chatRepository.saveAssistantMessage(
                    conversationId = conversationId,
                    content = "$partial\n\n*(Generation stopped)*",
                    isDemoResponse = isDemo
                )
            }
            _ephemeralState.update {
                it.copy(isGenerating = false, streamingContent = "")
            }
        }
    }

    fun renameConversation(newTitle: String) {
        viewModelScope.launch {
            chatRepository.renameConversation(conversationId, newTitle)
        }
    }

    fun clearConversation() {
        generationJob?.cancel()
        viewModelScope.launch {
            chatRepository.clearConversationMessages(conversationId)
            _ephemeralState.update {
                it.copy(
                    isGenerating = false,
                    streamingContent = "",
                    errorMessage = null,
                    lastFailedPrompt = null
                )
            }
        }
    }

    fun deleteConversation(onDeleted: () -> Unit) {
        generationJob?.cancel()
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)
            onDeleted()
        }
    }

    class Factory(
        private val conversationId: String,
        private val initialMode: AiMode,
        private val initialPrompt: String?,
        private val chatRepository: ChatRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(
                conversationId = conversationId,
                initialMode = initialMode,
                initialPrompt = initialPrompt,
                chatRepository = chatRepository,
                preferencesRepository = preferencesRepository
            ) as T
        }
    }
}
