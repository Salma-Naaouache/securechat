package com.student.securechat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.student.securechat.R
import com.student.securechat.ui.home.HomeActivity
// ✅ AJOUTER CES IMPORTS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class SignupActivity : AppCompatActivity() {

    // ✅ REMPLACER AuthHelper par Firebase directement
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // ✅ INITIALISER Firebase Auth et Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Garder tes IDs actuels
        val displayNameEditText = findViewById<EditText>(R.id.displayNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)

        signupButton.setOnClickListener {
            val displayName = displayNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validation
            if (displayName.isEmpty()) {
                Toast.makeText(this, "Entrez un nom d'utilisateur", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Entrez un email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ CRÉER LE COMPTE avec Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // ✅ RÉCUPÉRER LE userId
                        val currentUser = auth.currentUser
                        val userId = currentUser?.uid ?: return@addOnCompleteListener

                        // ✅ CRÉER L'UTILISATEUR dans Firestore (collection "users")
                        createUserInFirestore(userId, email, displayName)

                    } else {
                        // Erreur lors de la création du compte
                        Toast.makeText(
                            this,
                            "Erreur: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    /**
     * ✅ NOUVELLE FONCTION : Créer l'utilisateur dans Firestore
     */
    private fun createUserInFirestore(userId: String, email: String, displayName: String) {
        // Créer l'objet User selon notre structure Firebase
        val userData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "avatarUrl" to "",  // Vide pour l'instant
            "publicKey" to "",  // Tu généreras la clé RSA plus tard
            "createdAt" to FieldValue.serverTimestamp(),
            "lastSeen" to FieldValue.serverTimestamp(),
            "isOnline" to true
        )

        // ✅ ENVOYER à Firebase dans la collection "users"
        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                // ✅ SUCCÈS : Sauvegarder localement et aller à HomeActivity
                saveUserDataLocally(userId, displayName)

                Toast.makeText(
                    this,
                    "Bienvenue $displayName !",
                    Toast.LENGTH_SHORT
                ).show()

                // Aller vers HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USER_ID", userId)
                intent.putExtra("DISPLAY_NAME", displayName)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // ❌ ERREUR Firestore
                Toast.makeText(
                    this,
                    "Erreur Firestore: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * ✅ SAUVEGARDER les données localement (SharedPreferences)
     */
    private fun saveUserDataLocally(userId: String, displayName: String) {
        val sharedPref = getSharedPreferences("SecureChatPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_ID", userId)
            putString("DISPLAY_NAME", displayName)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }
}