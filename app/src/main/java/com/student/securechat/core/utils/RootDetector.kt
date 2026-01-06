package com.student.securechat.core.utils

import android.os.Build
import java.io.File

object RootDetector {

    /**
     * 1. VÉRIFICATION DU BINAIRE "SU"
     * On parcourt les répertoires standards où les gestionnaires de root
     * (comme Magisk ou SuperSU) installent le binaire "su".
     */
    private val binaryPaths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/com/student/securechat/data/local/xbin/su",
        "/com/student/securechat/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/com/student/securechat/data/local/su"
    )

    /**
     * 2. VÉRIFICATION DES TAGS DE BUILD
     * On vérifie si le système d'exploitation a été compilé avec des clés de test.
     * Build.TAGS est une constante système fournie par Android.
     */
    private fun checkBuildTags(): Boolean {
        val tags = Build.TAGS
        return tags != null && tags.contains("test-keys")
    }

    /**
     * FONCTION PRINCIPALE D'ACTION
     * Combine les vérifications pour retourner un verdict.
     */
    fun isDeviceRooted(): Boolean {
        // Vérifie la présence physique des fichiers 'su'
        for (path in binaryPaths) {
            if (File(path).exists()) return true
        }

        // Vérifie les signatures du firmware
        if (checkBuildTags()) return true

        return false
    }
}