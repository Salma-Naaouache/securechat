package com.student.securechat

import android.os.Bundle
import androidx.appcompat.app.AlertDialog // Import pour le dialogue
import androidx.appcompat.app.AppCompatActivity
import com.student.securechat.core.utils.RootDetector // Import de votre détecteur
import com.student.securechat.core.security.BiometricAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- ÉTAPE 1 : VÉRIFICATION DU ROOT ---
        if (RootDetector.isDeviceRooted()) {
            showRootWarning()
        } else {
            // --- ÉTAPE 2 : SI PAS ROOTÉ, ON LANCE LA BIOMÉTRIE ---
            startBiometricCheck()

        }
    }

    private fun showRootWarning() {
        AlertDialog.Builder(this)
            .setTitle("Sécurité Compromise")
            .setMessage("Votre appareil semble être rooté. Pour garantir la sécurité 'Zero-Trust' de vos messages, cette application ne peut pas s'exécuter.")
            .setCancelable(false) // L'utilisateur ne peut pas fermer la fenêtre en cliquant à côté
            .setPositiveButton("Quitter") { _, _ ->
                finish() // Ferme l'application
            }
            .show()
    }

    private fun startBiometricCheck() {
        val biometricAuth = BiometricAuth(this)
        if (biometricAuth.isBiometricAvailable()) {
            biometricAuth.authenticate {
                // Succès : L'utilisateur est identifié, on peut charger les messages
                runOnUiThread {
                    // Afficher votre layout ou rediriger vers l'accueil
                }
            }
        }
    }
}