package com.example.mixnchat.ui.login.signin

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginViewModel : ViewModel() {

    lateinit var auth : FirebaseAuth
    var errorMessage : String ?= null
    var errorMessageMail : String ?= null

    fun init (){
        auth = Firebase.auth

    }

    fun signIn(
        email : String,
        password : String,
        onError : (String) -> Unit,
        onSuccess : () -> Unit
    ){
        if(email == "" || password == ""){
            onError(errorMessage!!)

        }else{
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                if (auth.currentUser!!.isEmailVerified){
                   onSuccess()
                }else{
                    onError(errorMessageMail!!)
                }
            }.addOnFailureListener {
                it.localizedMessage?.let { it1 ->
                    onError(it1) }
            }
        }
    }
}