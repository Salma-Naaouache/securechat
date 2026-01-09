package com.student.securechat.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.student.securechat.R
import com.student.securechat.data.model.Message
import com.student.securechat.data.model.User
import com.student.securechat.security.CryptoManager
import java.io.PrintWriter
import java.io.StringWriter
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var errorView: TextView
    private lateinit var chatContentView: ConstraintLayout

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageButton

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var cryptoManager: CryptoManager

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var chatRoomId: String? = null
    private lateinit var recipient: User
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        errorView = findViewById(R.id.error_view)
        chatContentView = findViewById(R.id.chat_content_view)

        try {
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            cryptoManager = CryptoManager(this)

            val recipientId = intent.getStringExtra("RECIPIENT_ID")
            val currentUserId = auth.currentUser?.uid

            if (recipientId == null || currentUserId == null) {
                showError("Error: User IDs are invalid.")
                return
            }

            initializeViews()

            val currentUserDoc = db.collection("users").document(currentUserId).get()
            val recipientDoc = db.collection("users").document(recipientId).get()

            currentUserDoc.addOnSuccessListener { currentUserSnapshot ->
                recipientDoc.addOnSuccessListener { recipientSnapshot ->
                    if (!currentUserSnapshot.exists() || !recipientSnapshot.exists()) {
                        showError("Error: User document does not exist.")
                        return@addOnSuccessListener
                    }
                    try {
                        this.currentUser = currentUserSnapshot.toObject(User::class.java)!!.copy(userId = currentUserSnapshot.id)
                        this.recipient = recipientSnapshot.toObject(User::class.java)!!.copy(userId = recipientSnapshot.id)

                        setupToolbar()
                        setupRecyclerView()

                        getOrCreateChatRoom(recipientId) {
                            resetUnreadCount()
                            listenForMessages()
                        }

                        btnSend.setOnClickListener {
                            val messageContent = edtMessage.text.toString()
                            if (messageContent.isNotEmpty()) {
                                sendMessage(messageContent)
                            }
                        }
                    } catch (e: Exception) {
                        showError("Failed to parse user document", e)
                    }
                }.addOnFailureListener { showError("Failed to fetch recipient document", it) }
            }.addOnFailureListener { showError("Failed to fetch current user document", it) }
        } catch (e: Exception) {
            showError("A critical error occurred in onCreate", e)
        }
    }

    private fun resetUnreadCount() {
        val currentUserId = auth.currentUser?.uid ?: return
        chatRoomId?.let {
            val unreadCountUpdate = mapOf("unreadCount.$currentUserId" to 0)
            db.collection("chatRooms").document(it).update(unreadCountUpdate)
        }
    }

    private fun sendMessage(content: String) {
        edtMessage.text.clear()
        val currentUserId = currentUser.userId
        val recipientId = recipient.userId

        chatRoomId?.let { roomId ->
            val roomRef = db.collection("chatRooms").document(roomId)

            db.runTransaction { transaction ->
                val aesKey = cryptoManager.generateAesKey()
                
                val encryptedKeysMap = mutableMapOf<String, String>()

                val recipientPublicKey = cryptoManager.decodePublicKey(recipient.publicKey)
                val encryptedKeyForRecipient = cryptoManager.encryptWithRsa(cryptoManager.secretKeyToByteArray(aesKey), recipientPublicKey)
                encryptedKeysMap[recipientId] = android.util.Base64.encodeToString(encryptedKeyForRecipient, android.util.Base64.DEFAULT)

                val currentUserPublicKey = cryptoManager.decodePublicKey(currentUser.publicKey)
                val encryptedKeyForSender = cryptoManager.encryptWithRsa(cryptoManager.secretKeyToByteArray(aesKey), currentUserPublicKey)
                encryptedKeysMap[currentUserId] = android.util.Base64.encodeToString(encryptedKeyForSender, android.util.Base64.DEFAULT)

                val (iv, encryptedContent) = cryptoManager.encryptWithAes(content, aesKey)

                val newMessage = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = currentUserId,
                    iv = android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT),
                    content = android.util.Base64.encodeToString(encryptedContent, android.util.Base64.DEFAULT),
                    encryptedKeys = encryptedKeysMap
                )

                val messageRef = roomRef.collection("messages").document()
                transaction.set(messageRef, newMessage)

                val chatRoomUpdate = mapOf(
                    "lastMessage" to content,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "unreadCount.$recipientId" to FieldValue.increment(1),
                    "lastMessageSenderId" to currentUserId
                )
                transaction.update(roomRef, chatRoomUpdate)

                null
            }
        }
    }

    private fun showError(message: String, throwable: Throwable? = null) {
        Log.e("ChatActivity", message, throwable)
        chatContentView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        val sw = StringWriter()
        throwable?.printStackTrace(PrintWriter(sw))
        val exceptionAsString = sw.toString()
        errorView.text = "$message\n\n${exceptionAsString}"
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbarChat)
        toolbarTitle = findViewById(R.id.toolbarChatTitle)
        rvMessages = findViewById(R.id.rvMessages)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSendMessage)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarTitle.text = recipient.displayName
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(currentUser.userId, cryptoManager)
        rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun getOrCreateChatRoom(recipientId: String, onComplete: () -> Unit) {
        val currentUserId = auth.currentUser!!.uid
        val generatedId = generateChatRoomId(currentUserId, recipientId)
        chatRoomId = generatedId

        db.collection("chatRooms").document(generatedId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    val chatRoom = mapOf(
                        "participants" to listOf(currentUserId, recipientId),
                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "unreadCount" to mapOf(currentUserId to 0, recipientId to 0)
                    )
                    db.collection("chatRooms").document(generatedId).set(chatRoom)
                        .addOnSuccessListener { onComplete() }
                } else {
                    onComplete()
                }
            }
    }

    private fun generateChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    private fun listenForMessages() {
        chatRoomId?.let { roomId ->
            db.collection("chatRooms").document(roomId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        showError("Error listening for messages", e)
                        return@addSnapshotListener
                    }
                    snapshots?.let {
                        val messages = it.toObjects(Message::class.java)
                        chatAdapter.setMessages(messages)
                        rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
        }
    }
}