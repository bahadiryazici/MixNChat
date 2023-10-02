package com.example.mixnchat.ui.settings.editpassword

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EditPasswordViewModel : ViewModel() {
    private lateinit var auth : FirebaseAuth
    fun init(){
        auth = Firebase.auth
    }
    fun updatePassword(
        newPassword : String,
        authCredential: AuthCredential,
        onError : (String) -> Unit,
        onSuccess : () -> Unit){
        auth.currentUser!!.reauthenticate(authCredential).addOnSuccessListener {
            auth.currentUser!!.updatePassword(newPassword).addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                it.localizedMessage?.let { it1 -> onError(it1) }
            }
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
}