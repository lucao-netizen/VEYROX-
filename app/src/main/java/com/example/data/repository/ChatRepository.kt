package com.example.data.repository

import com.example.data.ai.AiChatTurn
import com.example.data.ai.AiRequest
import com.example.data.ai.AiServiceRouter
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.BackendHealthResponseDto
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.MessageRole
import com.example.domain.model.AiEngineType
import com.example.domain.model.AiMode
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val aiServiceRouter: AiServiceRouter
) {
    fun observeAllConversations(): Flow<List<ConversationEntity>> =
        chatDao.observeAllConversations()

    fun observeRecentConversations(limit: Int = 5): Flow<List<ConversationEntity>> =
        chatDao.observeRecentConversations(limit)

    fun searchConversations(query: String): Flow<List<ConversationEntity>> =
        if (query.isBlank()) chatDao.observeAllConversations()
        else chatDao.searchConversations(query.trim())

    fun observeConversation(conversationId: String): Flow<ConversationEntity?> =
        chatDao.observeConversation(conversationId)

    fun observeMessages(conversationId: String): Flow<List<MessageEntity>> =
        chatDao.observeMessages(conversationId)

    suspend fun createConversation(
        mode: AiMode = AiMode.GENERAL,
        initialTitle: String = "New ${mode.displayName} Chat"
    ): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        chatDao.insertConversation(
            ConversationEntity(
                id = id,
                title = initialTitle,
                modeName = mode.name,
                createdAt = now,
                updatedAt = now,
                lastMessagePreview = "",
                messageCount = 0
            )
        )
        return id
    }

    suspend fun renameConversation(conversationId: String, newTitle: String) {
        val cleaned = newTitle.trim()
        if (cleaned.isNotEmpty()) {
            chatDao.renameConversation(conversationId, cleaned, System.currentTimeMillis())
        }
    }

    suspend fun updateConversationMode(conversationId: String, mode: AiMode) {
        chatDao.updateConversationMode(conversationId, mode.name, System.currentTimeMillis())
    }

    suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteConversation(conversationId)
    }

    suspend fun clearConversationMessages(conversationId: String) {
        chatDao.clearMessagesForConversation(conversationId)
        val existing = chatDao.getConversationById(conversationId) ?: return
        chatDao.updateConversation(
            existing.copy(
                updatedAt = System.currentTimeMillis(),
                lastMessagePreview = "",
                messageCount = 0
            )
        )
    }

    suspend fun deleteAllConversations() {
        chatDao.deleteAllConversations()
    }

    suspend fun saveUserMessage(conversationId: String, content: String, mode: AiMode): MessageEntity {
        val now = System.currentTimeMillis()
        var existing = chatDao.getConversationById(conversationId)
        if (existing == null) {
            existing = ConversationEntity(
                id = conversationId,
                title = generateAutoTitle(content),
                modeName = mode.name,
                createdAt = now,
                updatedAt = now
            )
            chatDao.insertConversation(existing)
        }

        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = MessageRole.USER.name,
            content = content,
            timestamp = now,
            isDemoResponse = false,
            isError = false
        )
        chatDao.insertMessage(message)

        val shouldAutoTitle = existing.messageCount == 0 && existing.title.startsWith("New ")
        val updatedTitle = if (shouldAutoTitle) generateAutoTitle(content) else existing.title
        chatDao.updateConversation(
            existing.copy(
                title = updatedTitle,
                modeName = mode.name,
                updatedAt = now,
                lastMessagePreview = content.take(100),
                messageCount = existing.messageCount + 1
            )
        )
        return message
    }

    suspend fun saveAssistantMessage(
        conversationId: String,
        content: String,
        isDemoResponse: Boolean,
        isError: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = MessageRole.ASSISTANT.name,
            content = content,
            timestamp = now,
            isDemoResponse = isDemoResponse,
            isError = isError
        )
        chatDao.insertMessage(message)

        val existing = chatDao.getConversationById(conversationId) ?: return
        chatDao.updateConversation(
            existing.copy(
                updatedAt = now,
                lastMessagePreview = content.replace(Regex("[#*`>]"), "").trim().take(100),
                messageCount = existing.messageCount + 1
            )
        )
    }

    suspend fun getHistoryTurns(conversationId: String): List<AiChatTurn> {
        return chatDao.getMessagesOnce(conversationId)
            .filter { !it.isError }
            .map {
                AiChatTurn(
                    isUser = it.messageRole == MessageRole.USER,
                    text = it.content
                )
            }
    }

    fun streamAiReply(
        engineType: AiEngineType,
        prompt: String,
        history: List<AiChatTurn>,
        mode: AiMode,
        streaming: Boolean,
        customBackendUrl: String
    ): Flow<AiStreamEvent> {
        return aiServiceRouter.streamChat(
            engineType = engineType,
            request = AiRequest(
                prompt = prompt,
                history = history,
                mode = mode,
                streaming = streaming,
                customBackendUrl = customBackendUrl
            )
        )
    }

    suspend fun checkCustomBackendHealth(url: String): Result<BackendHealthResponseDto> {
        return aiServiceRouter.pingCustomBackend(url)
    }

    private fun generateAutoTitle(prompt: String): String {
        val normalized = prompt.trim().replace(Regex("\\s+"), " ")
        if (normalized.length <= 40) return normalized
        val truncated = normalized.take(40)
        val lastSpace = truncated.lastIndexOf(' ')
        return if (lastSpace > 18) truncated.substring(0, lastSpace) + "..." else "$truncated..."
    }
}
