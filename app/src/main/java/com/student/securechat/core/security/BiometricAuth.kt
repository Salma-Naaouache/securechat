package com.student.securechat.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuth(private val activity: FragmentActivity) {

    // 1. Configuration du message à afficher
    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authentification requise")
        .setSubtitle("Accédez à vos messages sécurisés")
        .setNegativeButtonText("Annuler")
        .build()

    // 2. Vérification si la biométrie est disponible
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // 3. Lancement de l'authentification
    fun authenticate(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // LOGIQUE : Déverrouillez l'accès
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // LOGIQUE : Fermer l'application en cas d'erreur
                activity.finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Tentative échouée (doigt mal placé), on ne fait rien, l'utilisateur peut réessayer
            }
        })

        biometricPrompt.authenticate(promptInfo)
    }
}