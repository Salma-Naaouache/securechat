package com.student.securechat.ui.call

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.R
import com.student.securechat.data.model.User

class CallActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var callUserAdapter: CallUserAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCall)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rvUsers = findViewById(R.id.rvCallUsers)
        setupRecyclerView()
        loadUsers()
    }

    private fun setupRecyclerView() {
        callUserAdapter = CallUserAdapter(userList)
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = callUserAdapter
    }

    private fun loadUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").get().addOnSuccessListener { result ->
            val users = result.documents.mapNotNull { document ->
                val user = document.toObject(User::class.java)?.copy(userId = document.id)
                if (user?.userId == currentUserId) null else user
            }
            userList.clear()
            userList.addAll(users)
            callUserAdapter.notifyDataSetChanged()
        }
    }
}