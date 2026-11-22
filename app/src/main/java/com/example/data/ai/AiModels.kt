package com.example.data.ai

import com.example.domain.model.AiMode

data class AiChatTurn(
    val isUser: Boolean,
    val text: String
)

data class AiRequest(
    val prompt: String,
    val history: List<AiChatTurn>,
    val mode: AiMode,
    val streaming: Boolean,
    val customBackendUrl: String = ""
)

sealed interface AiProviderError {
    val userMessage: String

    data object EmptyMessage : AiProviderError {
        override val userMessage: String = "Please enter a message before sending."
    }

    data object NoInternet : AiProviderError {
        override val userMessage: String =
            "No internet connection detected. Check your network or switch to Demo Mode in Settings."
    }

    data object Timeout : AiProviderError {
        override val userMessage: String =
            "AI request timed out after 60 seconds. Please try again."
    }

    data class InvalidConfiguration(val details: String) : AiProviderError {
        override val userMessage: String = details
    }

    data class ServiceUnavailable(val details: String) : AiProviderError {
        override val userMessage: String = details
    }
}

sealed interface AiStreamEvent {
    data class Chunk(val delta: String) : AiStreamEvent
    data class Completed(val fullText: String, val isDemo: Boolean) : AiStreamEvent
    data class Error(val error: AiProviderError) : AiStreamEvent
}
