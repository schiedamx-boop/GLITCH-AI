package com.example.data.model

import android.graphics.Bitmap
import android.net.Uri

enum class AttachmentType {
    IMAGE,
    AUDIO
}

data class ChatAttachment(
    val type: AttachmentType,
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val mimeType: String = "image/jpeg",
    val displayName: String = "Bestand",
    val base64Data: String? = null
)
