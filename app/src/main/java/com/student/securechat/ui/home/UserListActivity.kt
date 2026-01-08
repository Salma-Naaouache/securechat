package com.student.securechat.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.R
import com.student.securechat.data.model.User // Assurez-vous que votre model User est bien ici

class UserListActivity : AppCompatActivity() {

    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private val filteredList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // 1. Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarUsers)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nouveau message"

        // 2. Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rvUserList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // On initialise l'adapter avec la liste filtrée
        userAdapter = UserAdapter(filteredList)
        recyclerView.adapter = userAdapter

        // 3. Setup Search Bar
        setupSearchBar()

        // 4. Charger les données
        fetchUsersFromFirestore()
    }

    private fun fetchUsersFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for (doc in documents) {
                    val user = doc.toObject(User::class.java)
                    // Optionnel : ne pas s'afficher soi-même dans la liste
                    userList.add(user)
                }
                filteredList.clear()
                filteredList.addAll(userList)
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearchBar() {
        val searchBar = findViewById<EditText>(R.id.searchUser)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(text: String) {
        filteredList.clear()
        if (text.isEmpty()) {
            filteredList.addAll(userList)
        } else {
            val query = text.lowercase()
            for (user in userList) {
                if (user.username.lowercase().contains(query)) {
                    filteredList.add(user)
                }
            }
        }
        userAdapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}