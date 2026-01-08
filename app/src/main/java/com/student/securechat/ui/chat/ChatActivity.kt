package com.student.securechat.ui.chat

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.student.securechat.R
import com.student.securechat.data.model.Message
import com.student.securechat.data.model.User
import com.student.securechat.security.CryptoManager
import java.util.UUID

class ChatActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cryptoManager = CryptoManager(this)

        recipient = intent.getParcelableExtra<User>("USER") ?: return

        initializeViews()
        setupToolbar()
        setupRecyclerView()

        getOrCreateChatRoom() {
            listenForMessages()
        }

        btnSend.setOnClickListener {
            val messageContent = edtMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                sendMessage(messageContent)
                edtMessage.text.clear()
            }
        }
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
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(auth.currentUser!!.uid, cryptoManager)
        rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun getOrCreateChatRoom(onComplete: () -> Unit) {
        val currentUserId = auth.currentUser!!.uid
        val recipientId = recipient.userId

        chatRoomId = generateChatRoomId(currentUserId, recipientId)

        db.collection("chatRooms").document(chatRoomId!!).get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    val chatRoom = mapOf("participants" to listOf(currentUserId, recipientId))
                    db.collection("chatRooms").document(chatRoomId!!).set(chatRoom)
                }
                onComplete()
            }
    }

    private fun generateChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    private fun listenForMessages() {
        chatRoomId?.let {
            db.collection("chatRooms").document(it).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show()
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

    private fun sendMessage(content: String) {
        val aesKey = cryptoManager.generateAesKey()
        val recipientPublicKey = cryptoManager.decodePublicKey(recipient.publicKey)

        val encryptedAesKey = cryptoManager.encryptWithRsa(cryptoManager.secretKeyToByteArray(aesKey), recipientPublicKey)
        val (iv, encryptedContent) = cryptoManager.encryptWithAes(content, aesKey)

        val message = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = auth.currentUser!!.uid,
            iv = android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT),
            content = android.util.Base64.encodeToString(encryptedContent, android.util.Base64.DEFAULT),
            encryptedAesKey = android.util.Base64.encodeToString(encryptedAesKey, android.util.Base64.DEFAULT)
        )

        chatRoomId?.let {
            db.collection("chatRooms").document(it).collection("messages")
                .add(message)
        }
    }
}