package com.student.securechat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.student.securechat.MainActivity
import com.student.securechat.R
import com.student.securechat.data.local.SecureStorage // Import ajouté
import com.student.securechat.data.remote.AuthHelper

class SignupActivity : AppCompatActivity() {

    // On déclare le SecureStorage comme dans la LoginActivity
    private lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialisation du SecureStorage
        secureStorage = SecureStorage(this)

        val emailField = findViewById<EditText>(R.id.editEmail)
        val passwordField = findViewById<EditText>(R.id.editPassword)
        val signupBtn = findViewById<Button>(R.id.btnSignup)

        signupBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val pass = passwordField.text.toString().trim()

            if (email.isNotEmpty() && pass.length >= 6) {
                AuthHelper.signUp(email, pass) { success ->
                    if (success) {
                        // --- AJOUT DE LA SAUVEGARDE LOCALE ---
                        val uid = AuthHelper.getCurrentUserId() ?: "unknown"

                        // Utilisation de la nouvelle signature : saveDisplayName(userId, name)
                        secureStorage.saveDisplayName(uid, "User_$uid")

                        Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Erreur lors de l'inscription.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Veuillez remplir correctement les champs (Pass: 6 char min)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}