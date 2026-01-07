package com.student.securechat.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {
    // Création d'une clé maîtresse pour chiffrer le fichier de préférences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveDisplayName(userId: String, name: String) {
        sharedPrefs.edit().putString("${userId}_name", name).apply()
    }

    fun getDisplayName(userId: String): String? {
        return sharedPrefs.getString("${userId}_name", null)
    }}