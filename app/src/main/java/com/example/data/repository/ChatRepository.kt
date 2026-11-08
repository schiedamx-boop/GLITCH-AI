package com.example.data.repository

import com.example.data.db.ChatMessageEntity
import com.example.data.db.GlitchDao
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository interface for persisting and retrieving conversation history.
 */
interface ChatRepository {
    fun getConversationHistory(): Flow<List<ChatMessage>>
    suspend fun getAllHistoricalMessages(): List<ChatMessage>
    suspend fun getRecentHistoricalMessages(limit: Int = 30): List<ChatMessage>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun saveMessages(messages: List<ChatMessage>)
    suspend fun clearConversation()
    suspend fun getMessageCount(): Int
}

/**
 * Concrete implementation of [ChatRepository] backed by Room database via [GlitchDao].
 */
class RoomChatRepository(
    private val glitchDao: GlitchDao
) : ChatRepository {

    override fun getConversationHistory(): Flow<List<ChatMessage>> {
        return glitchDao.getAllMessages().map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    override suspend fun getAllHistoricalMessages(): List<ChatMessage> {
        return glitchDao.getAllMessagesList().map { it.toChatMessage() }
    }

    override suspend fun getRecentHistoricalMessages(limit: Int): List<ChatMessage> {
        // Query recent messages from database (ordered by timestamp asc for natural prompt order)
        val entities = glitchDao.getRecentMessages(limit)
        return entities.reversed().map { it.toChatMessage() }
    }

    override suspend fun saveMessage(message: ChatMessage) {
        glitchDao.insertMessage(ChatMessageEntity.fromChatMessage(message))
    }

    override suspend fun saveMessages(messages: List<ChatMessage>) {
        glitchDao.insertMessages(messages.map { ChatMessageEntity.fromChatMessage(it) })
    }

    override suspend fun clearConversation() {
        glitchDao.clearAllMessages()
    }

    override suspend fun getMessageCount(): Int {
        return glitchDao.getMessageCount()
    }
}
