package com.student.securechat.data.model

import com.google.firebase.firestore.FieldValue

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val iv: String = "", // Base64 encoded Initialization Vector
    val content: String = "", // Base64 encoded encrypted message content
    val encryptedAesKey: String = "", // Base64 encoded, RSA-encrypted AES key
    val timestamp: Any = FieldValue.serverTimestamp()
)