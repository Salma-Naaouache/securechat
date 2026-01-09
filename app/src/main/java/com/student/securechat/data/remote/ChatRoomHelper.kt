package com.student.securechat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.data.model.ChatRoom
import com.student.securechat.data.model.User

class ChatRoomHelper(private val db: FirebaseFirestore) {

    fun createChatRoom(currentUserId: String, otherUser: User, onComplete: (String) -> Unit) {
        val chatRoomId = generateChatRoomId(currentUserId, otherUser.userId)

        val chatRoom = ChatRoom(
            chatRoomId = chatRoomId,
            // ✅ CORRIGÉ: Utiliser une List comme attendu par le modèle
            participants = listOf(currentUserId, otherUser.userId) 
        )

        db.collection("chatRooms").document(chatRoomId)
            .set(chatRoom)
            .addOnSuccessListener {
                onComplete(chatRoomId)
            }
    }

    private fun generateChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }
}