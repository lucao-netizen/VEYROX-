package com.example.data.ai

import kotlinx.coroutines.flow.Flow

interface AiProvider {
    val providerId: String
    val isDemo: Boolean
    fun streamChat(request: AiRequest): Flow<AiStreamEvent>
}
