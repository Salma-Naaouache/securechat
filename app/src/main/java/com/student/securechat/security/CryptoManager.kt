package com.student.securechat.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager(context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    companion object {
        private const val RSA_ALIAS = "RSA_KEY_ALIAS"
        private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val RSA_ALGORITHM = "RSA/ECB/PKCS1Padding"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    }

    fun getOrCreateRsaPublicKey(): PublicKey {
        return if (keyStore.containsAlias(RSA_ALIAS)) {
            keyStore.getCertificate(RSA_ALIAS).publicKey
        } else {
            generateRsaKeyPair().public
        }
    }

    private fun generateRsaKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            RSA_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(2048)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .build()

        keyPairGenerator.initialize(spec)
        return keyPairGenerator.generateKeyPair()
    }

    fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(RSA_ALIAS, null) as PrivateKey
    }

    fun encodePublicKey(publicKey: PublicKey): String {
        return android.util.Base64.encodeToString(publicKey.encoded, android.util.Base64.DEFAULT)
    }

    fun decodePublicKey(encodedKey: String): PublicKey {
        val keyBytes = android.util.Base64.decode(encodedKey, android.util.Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    fun encryptWithRsa(data: ByteArray, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance(RSA_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }

    fun decryptWithRsa(encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        return cipher.doFinal(encryptedData)
    }

    fun generateAesKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    fun encryptWithAes(data: String, secretKey: SecretKey): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Pair(iv, encryptedData)
    }

    fun decryptWithAes(encryptedData: ByteArray, secretKey: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return String(cipher.doFinal(encryptedData))
    }

    fun secretKeyToByteArray(secretKey: SecretKey): ByteArray = secretKey.encoded

    fun byteArrayToSecretKey(byteArray: ByteArray): SecretKey = SecretKeySpec(byteArray, 0, byteArray.size, AES_ALGORITHM)
}