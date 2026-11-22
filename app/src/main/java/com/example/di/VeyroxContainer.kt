package com.example.di

import android.content.Context
import com.example.core.network.NetworkMonitor
import com.example.data.ai.AiServiceRouter
import com.example.data.ai.CustomBackendAiProvider
import com.example.data.ai.DemoAiProvider
import com.example.data.ai.FirebaseAiLogicProvider
import com.example.data.local.VeyroxDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ChatRepository

class VeyroxContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: VeyroxDatabase by lazy {
        VeyroxDatabase.getInstance(appContext)
    }

    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(appContext)
    }

    private val aiServiceRouter: AiServiceRouter by lazy {
        AiServiceRouter(
            networkMonitor = networkMonitor,
            firebaseProvider = FirebaseAiLogicProvider(),
            demoProvider = DemoAiProvider(),
            customBackendProvider = CustomBackendAiProvider()
        )
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(
            chatDao = database.chatDao(),
            aiServiceRouter = aiServiceRouter
        )
    }
}
