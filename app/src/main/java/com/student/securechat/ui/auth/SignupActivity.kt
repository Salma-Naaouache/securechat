package com.student.securechat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.student.securechat.R
import com.student.securechat.security.CryptoManager
import com.student.securechat.ui.home.HomeActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var cryptoManager: CryptoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        cryptoManager = CryptoManager(this)

        val displayNameEditText = findViewById<EditText>(R.id.displayNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val loginText = findViewById<TextView>(R.id.loginText)

        signupButton.setOnClickListener {
            val displayName = displayNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        createUserInFirestore(userId, email, displayName)
                    } else {
                        Toast.makeText(this, "Erreur: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun createUserInFirestore(userId: String, email: String, displayName: String) {
        val publicKey = cryptoManager.getOrCreateRsaPublicKey()
        val encodedPublicKey = cryptoManager.encodePublicKey(publicKey)

        Log.d("SignupActivity", "Generated Public Key for $displayName: $encodedPublicKey")

        if (encodedPublicKey.isEmpty()) {
            Log.e("SignupActivity", "Generated public key is EMPTY!")
        }

        val userData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "avatarUrl" to "",
            "publicKey" to encodedPublicKey,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastSeen" to FieldValue.serverTimestamp(),
            "isOnline" to true
        )

        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                saveUserDataLocally(userId, displayName)
                Toast.makeText(this, "Bienvenue $displayName !", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

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
