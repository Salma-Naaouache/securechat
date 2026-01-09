package com.student.securechat.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatSummary(
    val chatRoomId: String = "",
    // Pour les chats 1-to-1
    val otherParticipantId: String? = null,
    val otherParticipantName: String? = null,
    val otherParticipantAvatar: String? = null,
    // Pour tous les chats
    val lastMessage: String? = null,
    @ServerTimestamp val lastMessageTimestamp: Date? = null,
    val unreadCount: Map<String, Long> = emptyMap(),
    val lastMessageSenderId: String? = null,
    // Pour les groupes
    val isGroup: Boolean = false,
    val groupName: String? = null
)
