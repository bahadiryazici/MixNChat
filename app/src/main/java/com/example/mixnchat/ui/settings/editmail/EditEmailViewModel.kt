package com.example.mixnchat.ui.settings.editmail

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EditEmailViewModel : ViewModel() {

    private lateinit var auth : FirebaseAuth

    fun init(){
        auth = Firebase.auth
    }
    fun updateMail(
        newMail : String,
        authCredential: AuthCredential,
        onSuccess : () -> Unit,
        onError : (String) -> Unit){
        auth.currentUser!!.reauthenticate(authCredential).addOnSuccessListener {
            auth.currentUser!!.updateEmail(newMail).addOnSuccessListener {
                auth.currentUser!!.sendEmailVerification().addOnCompleteListener {
                    if(it.isSuccessful){
                        onSuccess()
                    }else{
                        val errorMessage = it.exception?.message
                        onError(errorMessage!!)
                    }
                }.addOnFailureListener {
                    it.localizedMessage?.let { it1 -> onError(it1) }
                }
            }.addOnFailureListener {
                it.localizedMessage?.let { it1 -> onError(it1) }
            }
        }
    }
}