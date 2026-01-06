package com.student.securechat

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Ici, vous initialiserez plus tard vos composants de sécurité
        // comme le KeyStore ou la détection de Root.
    }
}