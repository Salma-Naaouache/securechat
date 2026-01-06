package com.student.securechat.core.security

import android.util.Base64
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128 // Longueur d'authentification GCM standard

    // 1. Récupération de la clé AES via le Keystore
    private fun getKey() = KeyStoreHelper.getOrCreateKey()

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        // 3. Chiffrement
        val ciphertext = cipher.doFinal(data.toByteArray())

        // 4. On combine l'IV (cipher.iv) et le texte chiffré pour Firebase
        val byteBuffer = ByteBuffer.allocate(cipher.iv.size + ciphertext.size)
        byteBuffer.put(cipher.iv)
        byteBuffer.put(ciphertext)

        return Base64.encodeToString(byteBuffer.array(), Base64.DEFAULT)
    }

    fun decrypt(base64Data: String): String {
        val combined = Base64.decode(base64Data, Base64.DEFAULT)
        val byteBuffer = ByteBuffer.wrap(combined)

        // Extraction de l'IV (toujours 12 octets pour GCM par défaut)
        val iv = ByteArray(12)
        byteBuffer.get(iv)

        val ciphertext = ByteArray(byteBuffer.remaining())
        byteBuffer.get(ciphertext)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        return String(cipher.doFinal(ciphertext))
    }
}