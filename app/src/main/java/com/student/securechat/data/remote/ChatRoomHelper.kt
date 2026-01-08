package com.student.securechat.data.remote


import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.data.model.ChatRoom

/**
 * Classe helper pour gérer les ChatRooms
 */
object ChatRoomHelper {

    /**
     * Génère un ID de chatRoom unique entre deux utilisateurs
     * (toujours dans le même ordre alphabétique)
     */
    fun generateChatRoomId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${sortedIds[0]}_${sortedIds[1]}"
    }

    /**
     * Crée un nouveau ChatRoom dans Firestore
     * @param currentUserId ID de l'utilisateur actuel
     * @param otherUserId ID de l'autre utilisateur
     * @param callback Callback (success, chatRoomId ou errorMessage)
     */
    fun createChatRoom(
        currentUserId: String,
        otherUserId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val chatRoomId = generateChatRoomId(currentUserId, otherUserId)

        // Vérifier si le chatRoom existe déjà
        db.collection("chatRooms")
            .document(chatRoomId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // ChatRoom existe déjà
                    callback(true, chatRoomId)
                } else {
                    // Créer un nouveau ChatRoom
                    val chatRoom = ChatRoom(
                        chatRoomId = chatRoomId,
                        participants = mapOf(
                            currentUserId to true,
                            otherUserId to true
                        ),
                        participantsList = listOf(currentUserId, otherUserId),
                        lastMessage = null,
                        unreadCount = mapOf(
                            currentUserId to 0,
                            otherUserId to 0
                        )
                    )

                    db.collection("chatRooms")
                        .document(chatRoomId)
                        .set(chatRoom)
                        .addOnSuccessListener {
                            callback(true, chatRoomId)
                        }
                        .addOnFailureListener { e ->
                            callback(false, e.message)
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    /**
     * Récupère tous les chatRooms d'un utilisateur
     */
    fun getUserChatRooms(
        userId: String,
        callback: (Boolean, List<ChatRoom>?, String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("chatRooms")
            .whereArrayContains("participantsList", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    callback(false, null, error.message)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val chatRooms = snapshots.documents.mapNotNull { document ->
                        document.toObject(ChatRoom::class.java)
                    }
                    callback(true, chatRooms, null)
                } else {
                    callback(false, null, "Aucune donnée")
                }
            }
    }
}
