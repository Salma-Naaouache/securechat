package com.student.securechat.data.remote

import com.google.firebase.auth.FirebaseAuth

object AuthHelper {
    private val auth = FirebaseAuth.getInstance()

    fun signUp(email: String, pass: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { onResult(it.isSuccessful) }
    }

    fun signIn(email: String, pass: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { onResult(it.isSuccessful) }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}