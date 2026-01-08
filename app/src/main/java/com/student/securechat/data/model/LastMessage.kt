package com.student.securechat.data.model

import com.google.firebase.Timestamp

/**
 * Data Class pour le dernier message dans la preview du chat
 */
data class LastMessage(
    val encryptedContent: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val messageType: String = "text"  // "text", "image", "file"
)