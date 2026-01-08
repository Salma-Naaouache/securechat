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
import com.student.securechat.ui.home.HomeActivity

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
        val usernameField = findViewById<EditText>(R.id.editUsername)
        val signupBtn = findViewById<Button>(R.id.btnSignup)

        signupBtn.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val pass = passwordField.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && pass.length >= 6) {
                // On passe maintenant 3 paramètres à signUp
                AuthHelper.signUp(email, pass, username) { success ->
                    if (success) {
                        val uid = AuthHelper.getCurrentUserId() ?: "unknown"

                        // On sauvegarde le VRAI username dans SecureStorage au lieu de "User_$uid"
                        secureStorage.saveDisplayName(uid, username)

                        Toast.makeText(this, "Bienvenue $username !", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Erreur lors de l'inscription.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs correctement", Toast.LENGTH_SHORT).show()
            }
        }
    }
}