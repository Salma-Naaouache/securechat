package com.student.securechat.core.security
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyStoreHelper {

    private const val PROVIDER = "AndroidKeyStore" // L'instance du Keystore système
    private const val KEY_ALIAS = "SecureChatKey" // Le nom de votre clé dans le coffre

    /**
     * INITIALISATION : Accès à l'instance
     * On cherche le coffre-fort spécifique d'Android.
     */
    private val keyStore = KeyStore.getInstance(PROVIDER).apply {
        load(null)
    }

    /**
     * GÉNÉRATION & CONFIGURATION
     */
    fun getOrCreateKey(): SecretKey {
        // Vérifier si la clé existe déjà pour ne pas la recréer
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) return existingKey.secretKey

        // Si elle n'existe pas, on utilise KeyGenerator avec l'algorithme AES
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, // Algorithme AES demandé
            PROVIDER
        )

        // CONFIGURATION des paramètres (KeyGenParameterSpec)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM) // Mode GCM
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE) // Padding NONE
            .setUserAuthenticationRequired(false) // Peut être mis à true si lié à la biométrie
            .setRandomizedEncryptionRequired(true) // Sécurité accrue (IV aléatoire)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey() // La clé est générée dans le TEE
    }
}