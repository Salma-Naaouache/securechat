package com.student.securechat.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.student.securechat.R
import com.student.securechat.ui.auth.LoginActivity
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var imgProfileLarge: ImageView
    private lateinit var profileNameHeader: TextView
    private lateinit var txtProfileUsername: TextView
    private lateinit var txtProfileEmail: TextView
    private lateinit var btnEditPhoto: FloatingActionButton
    private lateinit var btnLogout: Button

    private var currentUserId: String? = null
    private var selectedImageUri: Uri? = null

    // Launcher pour sélectionner une image
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                uploadAvatarToFirebase(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialiser Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Récupérer userId
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialiser les views
        initializeViews()

        // Charger les données utilisateur
        loadUserProfile()

        // Bouton pour changer l'avatar
        btnEditPhoto.setOnClickListener {
            openImagePicker()
        }

        // Bottom Navigation
        setupBottomNavigation()

        // Logout
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun initializeViews() {
        imgProfileLarge = findViewById(R.id.imgProfileLarge)
        profileNameHeader = findViewById(R.id.profileNameHeader)
        txtProfileUsername = findViewById(R.id.txtProfileUsername)
        txtProfileEmail = findViewById(R.id.txtProfileEmail)
        btnEditPhoto = findViewById(R.id.btnEditPhoto)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadUserProfile() {
        currentUserId?.let { userId ->
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val displayName = document.getString("displayName") ?: "Utilisateur"
                        val email = document.getString("email") ?: ""
                        val avatarUrl = document.getString("avatarUrl") ?: ""

                        profileNameHeader.text = displayName
                        txtProfileUsername.text = displayName
                        txtProfileEmail.text = email

                        // Charger l'avatar si disponible
                        if (avatarUrl.isNotEmpty()) {
                            loadAvatarWithUri(Uri.parse(avatarUrl))
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uploadAvatarToFirebase(imageUri: Uri) {
        currentUserId?.let { userId ->
            Toast.makeText(this, "Upload en cours...", Toast.LENGTH_SHORT).show()

            val fileName = "avatars/${userId}_${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            storageRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val avatarUrl = downloadUri.toString()
                        updateAvatarInFirestore(avatarUrl)
                        loadAvatarWithUri(downloadUri)
                        Toast.makeText(this, "✅ Photo mise à jour !", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ Erreur: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun loadAvatarWithUri(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(imgProfileLarge)
    }

    private fun updateAvatarInFirestore(avatarUrl: String) {
        currentUserId?.let { userId ->
            db.collection("users")
                .document(userId)
                .update("avatarUrl", avatarUrl)
                .addOnSuccessListener {
                    println("✅ Avatar URL sauvegardée")
                }
                .addOnFailureListener { e ->
                    println("❌ Erreur: ${e.message}")
                }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationProfile)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chats -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_calls -> {
                    Toast.makeText(this, "Appels - Bientôt disponible", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun logout() {
        currentUserId?.let { userId ->
            db.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "isOnline" to false,
                        "lastSeen" to FieldValue.serverTimestamp()
                    )
                )
        }

        auth.signOut()

        val sharedPref = getSharedPreferences("SecureChatPrefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}