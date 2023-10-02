package com.example.mixnchat.ui.login.forgotpassword

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordViewModel : ViewModel() {

    private lateinit var auth: FirebaseAuth

    fun init(){
        auth = Firebase.auth
    }

    fun resetPassword(
        email : String,
        onSuccess : () -> Unit,
        onError : (String) -> Unit
    ){
        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
}