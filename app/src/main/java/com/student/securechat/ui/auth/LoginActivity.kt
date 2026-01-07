package com.student.securechat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.student.securechat.MainActivity
import com.student.securechat.R
import com.student.securechat.data.local.SecureStorage // Import du stockage local
import com.student.securechat.data.remote.AuthHelper
import com.student.securechat.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    // On déclare le SecureStorage
    private lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialisation du SecureStorage
        secureStorage = SecureStorage(this)

        val emailField = findViewById<EditText>(R.id.loginEmail)
        val passwordField = findViewById<EditText>(R.id.loginPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val goToSignup = findViewById<TextView>(R.id.txtGoToSignup)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val pass = passwordField.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                AuthHelper.signIn(email, pass) { success ->
                    if (success) {
                        // 1. On récupère l'UID depuis Firebase
                        val uid = AuthHelper.getCurrentUserId() ?: "unknown"

                        // 2. On appelle la fonction avec les DEUX paramètres (uid et le texte)
                        // C'est ici que 'name' et 'userId' sont maintenant correctement passés
                        secureStorage.saveDisplayName(uid, "User_$uid")

                        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Échec : Vérifiez vos identifiants", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}