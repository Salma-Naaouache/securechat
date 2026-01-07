package com.student.securechat.ui.home

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu // Important pour le menu custom
import com.student.securechat.R
import com.student.securechat.data.local.SecureStorage
import com.student.securechat.data.remote.AuthHelper

class HomeActivity : AppCompatActivity() {

    private lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        secureStorage = SecureStorage(this)

        // 1. Display dynamic welcome name (safe and formatted)
        val welcomeText = findViewById<TextView?>(R.id.txtWelcomeName)
        val uid = AuthHelper.getCurrentUserId().orEmpty()
        val storedName = secureStorage.getDisplayName(uid)
            ?.removePrefix("User_")
            ?.trim()
            .let { name ->
                when {
                    name == null || name.isBlank() -> "Guest"
                    else -> name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        welcomeText?.text = storedName

        // 2. Handle the "more" button if present; guard against missing view in layout
        val btnMore = findViewById<ImageButton?>(R.id.btnMoreOptions)
        btnMore?.setOnClickListener { view -> showPopupMenu(view) }
        btnMore?.contentDescription = "More options"
    }

    // Fonction pour afficher le menu "Nouveau groupe, Profil, Paramètres"
    private fun showPopupMenu(view: android.view.View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.main_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_group -> {
                    showInfoDialog("New group", "Create a new group — feature coming soon.")
                    true
                }
                R.id.action_profile -> {
                    showInfoDialog("Profile", "Open your profile to edit name and avatar.")
                    true
                }
                R.id.action_settings -> {
                    showInfoDialog("Settings", "Adjust app settings and preferences.")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}