package com.student.securechat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.student.securechat.core.utils.RootDetector
import com.student.securechat.ui.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Optionnel : Cacher la barre de statut pour un look "Full Screen"
        supportActionBar?.hide()

        // 1. Vérification de sécurité immédiate (Root)
        if (RootDetector.isDeviceRooted()) {
            // Si l'appareil est rooté, on peut choisir d'arrêter ici
            // ou d'afficher une alerte avant de fermer.
        }

        // 2. Attendre 3 secondes avant de passer au Login
        Handler(Looper.getMainLooper()).postDelayed({

            // Lancer la page de Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Très important : on "finish" la MainActivity pour que l'utilisateur
            // ne puisse pas revenir sur le Splash Screen avec le bouton retour.
            finish()

        }, 3000) // 3000 ms = 3 secondes
    }
}