package com.example.data.ai

import com.example.core.network.NetworkMonitor
import com.example.domain.model.AiEngineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AiServiceRouter(
    private val networkMonitor: NetworkMonitor,
    private val firebaseProvider: FirebaseAiLogicProvider,
    private val demoProvider: DemoAiProvider,
    private val customBackendProvider: CustomBackendAiProvider
) {
    fun streamChat(engineType: AiEngineType, request: AiRequest): Flow<AiStreamEvent> {
        if (request.prompt.isBlank()) {
            return flowOf(AiStreamEvent.Error(AiProviderError.EmptyMessage))
        }

        if (engineType != AiEngineType.DEMO_MODE && !networkMonitor.isOnline()) {
            return flowOf(AiStreamEvent.Error(AiProviderError.NoInternet))
        }

        val provider: AiProvider = when (engineType) {
            AiEngineType.FIREBASE_AI_LOGIC -> firebaseProvider
            AiEngineType.DEMO_MODE -> demoProvider
            AiEngineType.CUSTOM_BACKEND -> customBackendProvider
        }

        return provider.streamChat(request)
    }

    suspend fun pingCustomBackend(url: String): Result<BackendHealthResponseDto> {
        return customBackendProvider.pingHealth(url)
    }
}
