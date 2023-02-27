package com.cavdardevelopment.exchangr.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cavdardevelopment.exchangr.util.callbacks.ResultCallback
import com.cavdardevelopment.exchangr.util.exception.ConfirmPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel() : ViewModel() {
    private val auth = Firebase.auth.apply { useAppLanguage() }

    fun createAccount(email: String, password1: String, password2: String, callback: ResultCallback) {
        if (!passwordMatchCheck(password1, password2)) {
            val e = ConfirmPasswordException("Passwords do not match")
            Log.w(TAG_FIREBASE, "createAccount: confirm password", e)
            callback.onResult(false, "Passwords do not match")
            callback.onFailed(e)
            return
        }
        auth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG_FIREBASE, "createAccount: success")
                callback.onResult(true)
            }
            else {
                // If sign in fails, display a message to the user.
                Log.w(TAG_FIREBASE, "createAccount: failed ${task.exception?.message}", task.exception)
                callback.onResult(false, task.exception?.message)
                callback.onFailed(task.exception)
            }
        }
    }

    fun signIn(email: String, password: String, callback: ResultCallback) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG_FIREBASE, "signIn: success")
                callback.onResult(true)
            }
            else {
                // If sign in fails, display a message to the user.
                Log.w(TAG_FIREBASE, "signIn: failed ${task.exception?.message}", task.exception)
                callback.onResult(false, task.exception?.message)
                callback.onFailed(task.exception)
            }
        }
    }

    private fun passwordMatchCheck(password1: String, password2: String): Boolean = password1 == password2

    companion object {
        private const val TAG_FIREBASE = "firebase"
    }
}