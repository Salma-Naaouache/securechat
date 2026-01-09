package com.student.securechat.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.R
import com.student.securechat.adapters.UserAdapter
import com.student.securechat.data.model.User
import com.student.securechat.ui.chat.ChatActivity

class UserListActivity : AppCompatActivity(), UserAdapter.OnUserClickListener {

    private lateinit var rvUserList: RecyclerView
    private lateinit var searchUser: EditText
    private lateinit var userAdapter: UserAdapter
    private var userList: List<User> = listOf()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // ✅ Configuration de la Toolbar avec le bon ID
        val toolbar = findViewById<Toolbar>(R.id.toolbarUsers)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nouveau Message"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rvUserList = findViewById(R.id.rvUserList)
        searchUser = findViewById(R.id.searchUser)

        setupRecyclerView()
        loadUsers()

        searchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(userList, this)
        rvUserList.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = userAdapter
        }
    }

    private fun loadUsers() {
        db.collection("users").get().addOnSuccessListener { result ->
            userList = result.documents.mapNotNull { document ->
                val user = document.toObject(User::class.java)
                user?.copy(userId = document.id)
            }
            userAdapter.updateUsers(userList)
        }
    }

    private fun filterUsers(query: String) {
        val filteredList = userList.filter {
            it.displayName.contains(query, ignoreCase = true)
        }
        userAdapter.updateUsers(filteredList)
    }

    override fun onUserClick(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("RECIPIENT_ID", user.userId)
        startActivity(intent)
    }
}