package com.student.securechat.data.model

data class Message(
    val senderId: String = "",
    val content: String = "", // Ce sera le texte chiffré par CryptoManager
    val timestamp: Long = System.currentTimeMillis()
)