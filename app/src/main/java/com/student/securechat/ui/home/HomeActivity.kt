package com.student.securechat.ui.home

import android.os.Bundle
import android.view.Menu         // Import manquant dans ton erreur
import android.view.MenuItem     // Import manquant dans ton erreur
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.student.securechat.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Configuration de la Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // On retire le titre par défaut pour laisser le style personnalisé du XML
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    // Gestion du menu des 3 points (Correction des erreurs de ta photo)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_group -> {
                Toast.makeText(this, "Nouveau groupe", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_profile -> {
                Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Paramètres", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}