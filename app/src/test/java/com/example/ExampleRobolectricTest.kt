package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.config.VeyroxConfig
import com.example.core.network.NetworkMonitor
import com.example.data.ai.AiProviderError
import com.example.data.ai.AiRequest
import com.example.data.ai.AiServiceRouter
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.CustomBackendAiProvider
import com.example.data.ai.DemoAiProvider
import com.example.data.ai.FirebaseAiLogicProvider
import com.example.data.local.VeyroxDatabase
import com.example.data.repository.ChatRepository
import com.example.domain.model.AiEngineType
import com.example.domain.model.AiMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: VeyroxDatabase
    private lateinit var repository: ChatRepository
    private lateinit var aiServiceRouter: AiServiceRouter

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, VeyroxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        aiServiceRouter = AiServiceRouter(
            networkMonitor = NetworkMonitor(context),
            firebaseProvider = FirebaseAiLogicProvider(),
            demoProvider = DemoAiProvider(),
            customBackendProvider = CustomBackendAiProvider()
        )
        repository = ChatRepository(
            chatDao = database.chatDao(),
            aiServiceRouter = aiServiceRouter
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun brandingAndDiscordUrlMatchSpecification() {
        assertEquals("VEYROX", context.getString(R.string.app_name))
        assertEquals("https://discord.gg/WTctmpDXVc", VeyroxConfig.VEYROX_DISCORD_URL)
        assertEquals(4, AiMode.entries.size)
    }

    @Test
    fun conversationLifecyclePersistsAndRestoresCorrectly() = runTest {
        val convId = repository.createConversation(mode = AiMode.CODING)
        repository.saveUserMessage(
            conversationId = convId,
            content = "Write a clean Kotlin singleton pattern",
            mode = AiMode.CODING
        )
        repository.saveAssistantMessage(
            conversationId = convId,
            content = "Here is an object declaration in Kotlin.",
            isDemoResponse = true
        )

        val conversations = repository.observeAllConversations().first()
        assertEquals(1, conversations.size)
        assertEquals("Write a clean Kotlin singleton pattern", conversations.first().title)
        assertEquals(AiMode.CODING, conversations.first().aiMode)

        repository.renameConversation(convId, "Kotlin Singleton Guide")
        val renamed = repository.observeConversation(convId).first()
        assertEquals("Kotlin Singleton Guide", renamed?.title)

        val messages = repository.observeMessages(convId).first()
        assertEquals(2, messages.size)
        assertTrue(messages[1].isDemoResponse)

        repository.clearConversationMessages(convId)
        assertTrue(repository.observeMessages(convId).first().isEmpty())

        repository.deleteConversation(convId)
        assertTrue(repository.observeAllConversations().first().isEmpty())
    }

    @Test
    fun demoModeStreamsLabeledResponseAndEmptyInputReturnsError() = runTest {
        val emptyEvents = aiServiceRouter.streamChat(
            engineType = AiEngineType.DEMO_MODE,
            request = AiRequest(
                prompt = "   ",
                history = emptyList(),
                mode = AiMode.GENERAL,
                streaming = true
            )
        ).toList()

        assertEquals(1, emptyEvents.size)
        assertTrue(emptyEvents.first() is AiStreamEvent.Error)
        val err = (emptyEvents.first() as AiStreamEvent.Error).error
        assertTrue(err is AiProviderError.EmptyMessage)

        val demoEvents = aiServiceRouter.streamChat(
            engineType = AiEngineType.DEMO_MODE,
            request = AiRequest(
                prompt = "Summarize VEYROX capabilities",
                history = emptyList(),
                mode = AiMode.SUMMARIZER,
                streaming = false
            )
        ).toList()

        val completed = demoEvents.filterIsInstance<AiStreamEvent.Completed>().firstOrNull()
        assertTrue(completed != null)
        assertTrue(completed!!.isDemo)
        assertFalse(completed.fullText.isBlank())
        assertTrue(completed.fullText.contains("Demo Mode Active"))
    }
}
