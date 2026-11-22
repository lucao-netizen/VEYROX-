package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AiMode

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val modeName: String = AiMode.GENERAL.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessagePreview: String = "",
    val messageCount: Int = 0
) {
    val aiMode: AiMode
        get() = AiMode.fromName(modeName)
}
