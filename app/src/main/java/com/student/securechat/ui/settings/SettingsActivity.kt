package com.student.securechat.ui.settings

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.student.securechat.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbarSettings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        findViewById<TextView>(R.id.setting_account).setOnClickListener {
            showToast("Option Compte cliquée")
        }

        findViewById<TextView>(R.id.setting_notifications).setOnClickListener {
            showToast("Option Notifications cliquée")
        }

        findViewById<TextView>(R.id.setting_privacy).setOnClickListener {
            showToast("Option Confidentialité cliquée")
        }

        findViewById<TextView>(R.id.setting_help).setOnClickListener {
            showToast("Option Aide cliquée")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}