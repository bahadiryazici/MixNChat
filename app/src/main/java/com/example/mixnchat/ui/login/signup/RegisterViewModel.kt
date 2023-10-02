package com.example.mixnchat.ui.login.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.mixnchat.utils.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterViewModel : ViewModel() {

    private lateinit var auth: FirebaseAuth
    private val firebaseUtil = FirebaseUtil()
    var errorMessagePicture : String ?= null
    var errorMessageAll : String ?= null

    fun init(){
        auth = Firebase.auth
    }

    fun signUp(
        email : String,
        password : String,
        username : String,
        country : String,
        biography : String,
        gender : String,
        date : String,
        selectedPicture : Uri?,
        onSuccess : () -> Unit,
        onError : (String) -> Unit
        ){
        if (email.isEmpty() || password.isEmpty() ||biography.isEmpty()){
            onError(errorMessageAll!!)
            return
        }

        if (selectedPicture == null){
            onError(errorMessagePicture!!)
            return
        }

        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
            val imageName = "${firebaseUtil.currentUserId()}.jpg"
            val imageReference = firebaseUtil.getProfilePhotoFromStorage().child(imageName)
            auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    imageReference.putFile(selectedPicture).addOnSuccessListener {
                        imageReference.downloadUrl.addOnSuccessListener { uri->
                            val profileUrl = uri.toString()
                            val user = hashMapOf<String,Any>()
                            user["userUid"] = firebaseUtil.currentUserId()
                            user["profileUrl"] = profileUrl
                            user["username"] = username
                            user["date"] = date
                            user["country"] = country
                            user["gender"] = gender
                            user["biography"] = biography
                            user["speech"] = ""
                            firebaseUtil.getAllUser().document(firebaseUtil.currentUserId()).set(user).addOnSuccessListener {
                                onSuccess()
                            }.addOnFailureListener {
                                it.localizedMessage?.let { it1 -> onError(it1) }
                            }
                        }
                    }.addOnFailureListener {
                        it.localizedMessage?.let { it1 -> onError(it1) }
                    }
                }
            }

        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
}