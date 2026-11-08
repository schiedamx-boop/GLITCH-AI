package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AiEmotion
import com.example.data.model.AttachmentType
import com.example.data.model.ChatAttachment
import com.example.data.model.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val emotion: String,
    val attachmentType: String? = null,
    val attachmentName: String? = null,
    val attachmentUri: String? = null
) {
    fun toChatMessage(): ChatMessage {
        val parsedAttachment = if (attachmentType != null) {
            val type = if (attachmentType == "IMAGE") AttachmentType.IMAGE else AttachmentType.AUDIO
            val parsedUri = attachmentUri?.let { android.net.Uri.parse(it) }
            ChatAttachment(
                type = type,
                uri = parsedUri,
                displayName = attachmentName ?: "Bijlage"
            )
        } else null

        return ChatMessage(
            id = id,
            text = text,
            isUser = isUser,
            timestamp = timestamp,
            emotion = AiEmotion.fromString(emotion),
            attachment = parsedAttachment
        )
    }

    companion object {
        fun fromChatMessage(msg: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = msg.id,
                text = msg.text,
                isUser = msg.isUser,
                timestamp = msg.timestamp,
                emotion = msg.emotion.name,
                attachmentType = msg.attachment?.type?.name,
                attachmentName = msg.attachment?.displayName,
                attachmentUri = msg.attachment?.uri?.toString()
            )
        }
    }
}
