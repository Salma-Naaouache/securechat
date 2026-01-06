package com.student.securechat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.core.security.CryptoManager

object ChatService {
    private val db = FirebaseFirestore.getInstance()

    fun sendMessage(senderId: String, text: String) {
        // ÉTAPE CRUCIALE : Chiffrement avant l'envoi
        val encryptedText = CryptoManager.encrypt(text)

        val messageData = hashMapOf(
            "senderId" to senderId,
            "content" to encryptedText, // Seul le contenu chiffré est stocké
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("messages").add(messageData)
    }
}