package com.student.securechat.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.gms.tasks.Tasks
import com.student.securechat.R
import com.student.securechat.data.model.ChatSummary
import com.student.securechat.data.model.User
import com.student.securechat.ui.call.CallActivity
import com.student.securechat.ui.chat.ChatActivity
import com.student.securechat.ui.chat.GroupChatActivity
import com.student.securechat.ui.group.CreateGroupActivity
import com.student.securechat.ui.settings.SettingsActivity

class HomeActivity : AppCompatActivity(), RecentChatAdapter.OnChatClickListener {

    private lateinit var rvRecentChats: RecyclerView
    private lateinit var recentChatAdapter: RecentChatAdapter
    private lateinit var searchEditText: EditText
    private var fullChatList = listOf<ChatSummary>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            return
        }

        initializeViews()
        setupRecyclerView()
        listenForRecentChats()
        setupSearch()
    }

    private fun initializeViews() {
        rvRecentChats = findViewById(R.id.rvDiscussions)
        searchEditText = findViewById(R.id.searchEditText)

        val fabNewChat = findViewById<FloatingActionButton>(R.id.fabNewChat)
        fabNewChat.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Secure Chat"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_calls -> {
                    startActivity(Intent(this, CallActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        recentChatAdapter = RecentChatAdapter(emptyList(), this, currentUserId!!)
        rvRecentChats.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = recentChatAdapter
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChats(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterChats(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullChatList
        } else {
            fullChatList.filter {
                val nameToSearch = if (it.isGroup) it.groupName else it.otherParticipantName
                nameToSearch?.contains(query, ignoreCase = true) ?: false
            }
        }
        recentChatAdapter.updateChats(filteredList)
    }

    private fun listenForRecentChats() {
        val currentId = currentUserId ?: return

        db.collection("chatRooms")
            .whereArrayContains("participants", currentId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("HomeActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshots == null || snapshots.isEmpty) {
                    fullChatList = emptyList()
                    recentChatAdapter.updateChats(fullChatList)
                    return@addSnapshotListener
                }

                val otherUserIds = snapshots.documents.mapNotNull {
                    if (it.getBoolean("isGroup") != true) {
                        (it.get("participants") as? List<String>)?.firstOrNull { id -> id != currentId }
                    } else null
                }.distinct()

                val userFetchTask = if (otherUserIds.isNotEmpty()) {
                    db.collection("users").whereIn("userId", otherUserIds).get()
                } else {
                    Tasks.forResult(null)
                }

                userFetchTask.addOnSuccessListener { usersSnapshot ->
                    val usersMap = usersSnapshot?.documents?.mapNotNull { doc ->
                        try {
                            User(
                                userId = doc.id,
                                displayName = doc.getString("displayName") ?: "",
                                email = doc.getString("email") ?: "",
                                avatarUrl = doc.getString("avatarUrl") ?: "",
                                publicKey = doc.getString("publicKey") ?: "",
                                isOnline = doc.getBoolean("isOnline") ?: false,
                                createdAt = doc.getDate("createdAt"),
                                lastSeen = doc.getDate("lastSeen")
                            )
                        } catch (e: Exception) {
                            Log.e("HomeActivity", "Failed to parse user.", e)
                            null
                        }
                    }?.associateBy { it.userId } ?: emptyMap()

                    val newChatList = snapshots.documents.mapNotNull { doc ->
                        val isGroup = doc.getBoolean("isGroup") ?: false
                        val unreadCountRaw = doc.get("unreadCount") as? Map<String, Any> ?: emptyMap()
                        val unreadCount = unreadCountRaw.mapValues { (_, value) -> (value as? Number)?.toLong() ?: 0L }

                        if (isGroup) {
                            ChatSummary(
                                chatRoomId = doc.id,
                                isGroup = true,
                                groupName = doc.getString("groupName"),
                                lastMessage = doc.getString("lastMessage"),
                                lastMessageTimestamp = doc.getDate("lastMessageTimestamp"),
                                unreadCount = unreadCount,
                                lastMessageSenderId = doc.getString("lastMessageSenderId")
                            )
                        } else {
                            val otherId = (doc.get("participants") as? List<String>)?.firstOrNull { id -> id != currentId }
                            val otherUser = otherId?.let { usersMap[it] }
                            if (otherUser != null) {
                                ChatSummary(
                                    chatRoomId = doc.id,
                                    otherParticipantId = otherId,
                                    otherParticipantName = otherUser.displayName,
                                    otherParticipantAvatar = otherUser.avatarUrl,
                                    lastMessage = doc.getString("lastMessage"),
                                    lastMessageTimestamp = doc.getDate("lastMessageTimestamp"),
                                    unreadCount = unreadCount,
                                    lastMessageSenderId = doc.getString("lastMessageSenderId")
                                )
                            } else null
                        }
                    }.sortedByDescending { it.lastMessageTimestamp }

                    fullChatList = newChatList
                    filterChats(searchEditText.text.toString())
                }.addOnFailureListener {
                    Log.e("HomeActivity", "Failed to fetch user profiles", it)
                }
            }
    }

    override fun onChatClick(chatSummary: ChatSummary) {
        val intent = if (chatSummary.isGroup) {
            Intent(this, GroupChatActivity::class.java).apply {
                putExtra("CHAT_ROOM_ID", chatSummary.chatRoomId)
            }
        } else {
            Intent(this, ChatActivity::class.java).apply {
                putExtra("RECIPIENT_ID", chatSummary.otherParticipantId)
            }
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_group -> {
                startActivity(Intent(this, CreateGroupActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}