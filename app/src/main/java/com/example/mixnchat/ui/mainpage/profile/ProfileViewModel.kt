package com.example.mixnchat.ui.mainpage.profile


import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.mixnchat.data.Posts
import com.example.mixnchat.utils.FirebaseUtil
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val firebaseUtil = FirebaseUtil()
    var errorMessage : String ?= null
    fun getPosts(onError : (String) -> Unit, callBack : (ArrayList<Posts>) -> Unit){
        val postList = ArrayList<Posts>()
        firebaseUtil.getCurrentUserPosts().addSnapshotListener { value, error ->
            if (error != null){
                error.localizedMessage?.let { onError(it) }
            }

            if (value != null && !value.isEmpty){
                val documents = value.documents
                for (document in documents){
                    val postValue = document.getString("Post")
                    val postId = document.getString("Uid")
                    val post = Posts(postValue, postId)

                    postList.add(post)
                }
            }
            callBack(postList)
        }
    }
    fun uploadPhoto(onError: (String) -> Unit,onSuccess : () -> Unit, selectedPicture : Uri?){
        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"
        val imageReference = firebaseUtil.getPostsFromStorage().child(firebaseUtil.currentUserId()).child(imageName)

        if (selectedPicture!=null){
            imageReference.putFile(selectedPicture).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val postUri = uri.toString()
                    val uuidString = uuid.toString()
                    val userPost = hashMapOf<String,Any>()
                    userPost["Post"] = postUri
                    userPost["Uid"] = uuidString

                    firebaseUtil.getCurrentUserPosts().document(uuidString).set(userPost).addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener {
                        it.localizedMessage?.let { it1 -> onError(it1) }
                    }
                }.addOnFailureListener {
                    it.localizedMessage?.let { it1 -> onError(it1) }
                }
            }.addOnFailureListener{
                it.localizedMessage?.let { it1 -> onError(it1) }
            }
        }
    }
    fun backgroundPhoto(onError: (String) -> Unit,onSuccess : () -> Unit, backgroundPhoto : Uri?){
        val imageName = "${firebaseUtil.currentUserId()}.jpg"
        val imageReference = firebaseUtil.getBackgroundPhotosFromStorage().child(firebaseUtil.currentUserId()).child(imageName)
        if (backgroundPhoto!=null){
            imageReference.putFile(backgroundPhoto).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val userBackgroundImage = uri.toString()
                    val userBackground = hashMapOf<String,Any>()
                    userBackground["Background"] = userBackgroundImage

                    firebaseUtil.getCurrentUserBackground().document(firebaseUtil.currentUserId()).set(userBackground).addOnSuccessListener {
                        onSuccess()

                    }.addOnFailureListener {
                        it.localizedMessage?.let { it1 -> onError(it1) }
                    }
                }.addOnFailureListener {
                    it.localizedMessage?.let { it1 -> onError(it1) }
                }
            }.addOnFailureListener{
                it.localizedMessage?.let { it1 -> onError(it1) }
            }
        }
    }
    fun getBackgroundImage(onError: (String) -> Unit, onSuccess : (String) -> Unit){
        firebaseUtil.getCurrentUserBackground().document(firebaseUtil.currentUserId()).get().addOnSuccessListener {
            if(it.exists()){
                val backgroundImageUrl = it.getString("Background")
                if (backgroundImageUrl != null) {
                    onSuccess(backgroundImageUrl)
                }
            }
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
    fun getProfile(onError: (String) -> Unit, onSuccess : (ArrayList<String>) -> Unit){
        val list = ArrayList<String>()
        firebaseUtil.getAllUser().document(firebaseUtil.currentUserId()).get().addOnSuccessListener {
            if (it.exists()) {
                val profileUrl = it.getString("profileUrl")
                val username = it.getString("username")
                val country = it.getString("country")
                val biography = it.getString("biography")
                val speech = it.getString("speech")

                list.add(0, profileUrl!!)
                list.add(1, username!!)
                list.add(2, country!!)
                list.add(3, biography!!)
                list.add(4, speech!!)
                onSuccess(list)
            } else {
                onError(errorMessage!!)
            }
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
    fun getCurrentUserFollowing(onError: (String) -> Unit, onSuccess : (Int) -> Unit){
        firebaseUtil.getCurrentUserFollowing().get().addOnSuccessListener {
            val followingCount = it.size()
            onSuccess(followingCount)
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
    fun getCurrentUserFollowers(onError: (String) -> Unit, onSuccess : (Int) -> Unit){
        firebaseUtil.getCurrentUserFollowers().get().addOnSuccessListener {
            val followersCount = it.size()
            onSuccess(followersCount)
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
}