package com.student.securechat.data.remote

import com.google.firebase.auth.FirebaseAuth

object AuthHelper {
    private val auth = FirebaseAuth.getInstance()

    fun signUp(email: String, pass: String, username: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Si le compte est créé, on ajoute le nom d'utilisateur au profil Firebase
                    val user = auth.currentUser
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = username
                    }

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            callback(updateTask.isSuccessful)
                        }
                    // Si updateProfile échoue, on renvoie quand même true car le compte existe
                } else {
                    callback(false)
                }
            }
    }

    fun signIn(email: String, pass: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { onResult(it.isSuccessful) }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}