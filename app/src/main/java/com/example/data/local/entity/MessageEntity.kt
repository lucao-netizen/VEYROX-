package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MessageRole {
    USER,
    ASSISTANT;

    companion object {
        fun fromName(value: String): MessageRole {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ASSISTANT
        }
    }
}

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversation_id"])]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_demo_response") val isDemoResponse: Boolean = false,
    @ColumnInfo(name = "is_error") val isError: Boolean = false
) {
    val messageRole: MessageRole
        get() = MessageRole.fromName(role)
}
