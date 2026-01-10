package com.student.securechat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.student.securechat.core.utils.RootDetector
import com.student.securechat.ui.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        // ✅ UTILISATION DE ROOTBEER VIA NOTRE DÉTECTEUR
        if (RootDetector.isDeviceRooted(this)) {
            showRootDetectedDialog()
        } else {
            proceedToNextScreen()
        }
    }

    private fun showRootDetectedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sécurité compromise")
            .setMessage("L\'application ne peut pas fonctionner sur un appareil rooté pour des raisons de sécurité.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finishAffinity()
            }
            .show()
    }

    private fun proceedToNextScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}