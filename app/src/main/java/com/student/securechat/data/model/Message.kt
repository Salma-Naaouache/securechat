package com.student.securechat.data.model

import com.google.firebase.firestore.FieldValue

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val iv: String = "", // Base64 encoded Initialization Vector
    val content: String = "", // Base64 encoded encrypted message content
    
    // ✅ NOUVEAU format pour les groupes et les nouveaux messages
    val encryptedKeys: Map<String, String> = emptyMap(), // Map<UserId, EncryptedAESKey>
    
    // ✅ ANCIEN format pour la compatibilité ascendante des messages 1-to-1
    @Deprecated("Utiliser encryptedKeys à la place")
    val encryptedAesKey: String? = null,
    
    val timestamp: Any = FieldValue.serverTimestamp()
)
