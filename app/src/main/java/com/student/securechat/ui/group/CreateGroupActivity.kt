package com.student.securechat.ui.group

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.R
import com.student.securechat.data.model.User
import com.student.securechat.ui.home.HomeActivity

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var edtGroupName: EditText
    private lateinit var rvUsers: RecyclerView
    private lateinit var fabCreateGroup: FloatingActionButton
    private lateinit var userSelectAdapter: UserSelectAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val allUsers = mutableListOf<User>()
    private val selectedUsers = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        // ✅ Configuration de la Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        edtGroupName = findViewById(R.id.edtGroupName)
        rvUsers = findViewById(R.id.rvUserSelection)
        fabCreateGroup = findViewById(R.id.fabConfirmGroup)

        setupRecyclerView()
        loadUsers()

        fabCreateGroup.setOnClickListener {
            createGroup()
        }
    }

    private fun setupRecyclerView() {
        userSelectAdapter = UserSelectAdapter(allUsers) { userId, isSelected ->
            if (isSelected) {
                selectedUsers.add(userId)
            } else {
                selectedUsers.remove(userId)
            }
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userSelectAdapter
    }

    private fun loadUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").get().addOnSuccessListener { result ->
            val users = result.documents.mapNotNull { document ->
                val user = document.toObject(User::class.java)?.copy(userId = document.id)
                if (user?.userId == currentUserId) null else user
            }
            allUsers.addAll(users)
            userSelectAdapter.notifyDataSetChanged()
        }
    }

    private fun createGroup() {
        val groupName = edtGroupName.text.toString().trim()
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Veuillez donner un nom au groupe", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = auth.currentUser?.uid ?: return
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins un membre", Toast.LENGTH_SHORT).show()
            return
        }

        val participants = selectedUsers.toMutableList()
        participants.add(currentUserId)

        val chatRoomId = db.collection("chatRooms").document().id
        val newGroup = mapOf(
            "chatRoomId" to chatRoomId,
            "isGroup" to true,
            "groupName" to groupName,
            "participants" to participants,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "unreadCount" to participants.associateWith { 0L }
        )

        db.collection("chatRooms").document(chatRoomId).set(newGroup)
            .addOnSuccessListener {
                Toast.makeText(this, "Groupe '$groupName' créé", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur lors de la création du groupe", Toast.LENGTH_SHORT).show()
            }
    }
}