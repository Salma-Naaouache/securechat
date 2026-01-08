package com.student.securechat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.student.securechat.R
import com.student.securechat.ui.home.HomeActivity
// ✅ AJOUTER CES IMPORTS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // ✅ REMPLACER SecureStorage par Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ✅ INITIALISER Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailField = findViewById<EditText>(R.id.loginEmail)
        val passwordField = findViewById<EditText>(R.id.loginPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val goToSignup = findViewById<TextView>(R.id.txtGoToSignup)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            // Validation
            if (email.isEmpty()) {
                Toast.makeText(this, "Entrez votre email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Entrez votre mot de passe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ CONNEXION avec Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // ✅ RÉCUPÉRER le userId
                        val currentUser = auth.currentUser
                        val userId = currentUser?.uid

                        if (userId != null) {
                            // ✅ RÉCUPÉRER les infos de l'utilisateur depuis Firestore
                            loadUserDataAndLogin(userId)
                        } else {
                            Toast.makeText(this, "Erreur: Utilisateur non trouvé", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        // ❌ ERREUR de connexion
                        Toast.makeText(
                            this,
                            "Échec : ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    /**
     * ✅ FONCTION pour récupérer les données utilisateur depuis Firestore
     */
    private fun loadUserDataAndLogin(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // ✅ RÉCUPÉRER le displayName depuis Firestore
                    val displayName = document.getString("displayName") ?: "Utilisateur"
                    val email = document.getString("email") ?: ""

                    // Mettre à jour le statut "en ligne"
                    updateUserOnlineStatus(userId, true)

                    // ✅ SAUVEGARDER localement
                    saveUserDataLocally(userId, displayName, email)

                    Toast.makeText(this, "Bienvenue $displayName !", Toast.LENGTH_SHORT).show()

                    // ✅ ALLER vers HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    intent.putExtra("DISPLAY_NAME", displayName)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Utilisateur non trouvé dans la base", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * ✅ METTRE À JOUR le statut en ligne dans Firestore
     */
    private fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        val updates = hashMapOf<String, Any>(
            "isOnline" to isOnline,
            "lastSeen" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .update(updates)
    }

    /**
     * ✅ SAUVEGARDER les données localement (SharedPreferences)
     */
    private fun saveUserDataLocally(userId: String, displayName: String, email: String) {
        val sharedPref = getSharedPreferences("SecureChatPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_ID", userId)
            putString("DISPLAY_NAME", displayName)
            putString("EMAIL", email)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }

    /**
     * ✅ OPTIONNEL : Vérifier si l'utilisateur est déjà connecté
     */
    override fun onStart() {
        super.onStart()

        // Si déjà connecté, aller directement à HomeActivity
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}