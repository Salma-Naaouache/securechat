package com.student.securechat.data.model

data class ChatRoom(
    val chatRoomId: String = "",
    val participants: Map<String, Boolean> = emptyMap(),
    val participantsList: List<String> = emptyList(),
    val lastMessage: String? = null,
    val unreadCount: Map<String, Int> = emptyMap()
)
