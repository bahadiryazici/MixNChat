package com.example.mixnchat.ui.login.editprofile
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.mixnchat.utils.FirebaseUtil

class EditProfileViewModel : ViewModel() {
    private val firebaseUtil = FirebaseUtil()
    private var ppUrl : String ?= null
    private val imageName = "${firebaseUtil.currentUserId()}.jpg"
    private val imageReference = firebaseUtil.getProfilePhotoFromStorage().child(imageName)

    fun getProfile(onSuccess : (ArrayList<String>) -> Unit, onError : (String) -> Unit, callBack : (String) -> Unit){
        firebaseUtil.getAllUser().document(firebaseUtil.currentUserId()).get().addOnSuccessListener {
            if (it.exists()){
                ppUrl = it.getString("profileUrl").toString()
                val biography = it.getString("biography")
                val country = it.getString("country")
                val gender = it.getString("gender")
                val username = it.getString("username")
                val date = it.getString("date")
                val list = arrayListOf<String>()
                list.add(0,ppUrl!!)
                if (biography != null)
                    list.add(1,biography)
                else
                    list.add(1,"")
                if (country != null)
                    list.add(2,country)
                else
                    list.add(2,"")
                if (gender != null)
                    list.add(3,gender)
                else
                    list.add(3,"")
                if (username != null)
                    list.add(4,username)
                else
                    list.add(4,"")
                if (date != null)
                    list.add(5,date)
                else
                    list.add(5,"")
               onSuccess(list)
                callBack(ppUrl!!)
            }
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }

    fun uploadUser(onSuccess: () -> Unit, onError: (String) -> Unit){

        imageReference.delete().addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }

    fun uploadNewProfilePhoto(selectedPicture : Uri, onError: (String) -> Unit, onSuccess: (String) -> Unit){
        imageReference.putFile(selectedPicture).addOnSuccessListener {
            imageReference.downloadUrl.addOnSuccessListener { uri ->
                val ppUri = uri.toString()
                onSuccess(ppUri)
            }.addOnFailureListener {
                it.localizedMessage?.let { it1 -> onError(it1) }
            }
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }

    fun updateUserWithUri(
        ppUri: String,
        username:String,
        country : String,
        gender : String,
        biography:String,
        date : String,
        onError: (String) -> Unit,
        onSuccess: () -> Unit){

        val user = hashMapOf<String, Any>()
        user["userUid"] = firebaseUtil.currentUserId()
        user["profileUrl"] = ppUri
        user["username"] = username
        user["country"] = country
        user["date"] = date
        user["gender"] = gender
        user["biography"] = biography
        user["speech"] = ""

        firebaseUtil.getAllUser().document(firebaseUtil.currentUserId()).set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                it.localizedMessage?.let { it1 -> onError(it1) }
            }
    }
}