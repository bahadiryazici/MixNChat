package com.example.mixnchat.ui.mainpage.shuffle


import androidx.lifecycle.ViewModel
import com.example.mixnchat.data.Posts
import com.example.mixnchat.utils.FirebaseUtil

class ReviewedProfileViewModel : ViewModel() {

    private val firebaseUtil = FirebaseUtil()
    private var errorMessage : String ?= null
    fun setPosts(onError : (String) -> Unit, callback : (ArrayList<Posts>) -> Unit, userUid : String){
        val postList = ArrayList<Posts>()
        firebaseUtil.getOtherUserPosts(userUid).addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let{onError(it)}
            }
            if (value != null && !value.isEmpty) {
                val documents = value.documents

                for (document in documents) {
                    val postValue = document.getString("Post")
                    val postId = document.getString("Uid")
                    val post = Posts(postValue,postId)
                    postList.add(post)
                }
                callback(postList)
            }
        }
    }

    fun setReviewedProfile(userUid: String, callback: (ArrayList<String>) -> Unit, onError: (String) -> Unit){
        val list = ArrayList<String>()
        firebaseUtil.getAllUser().document(userUid).get().addOnSuccessListener {
            if(it.exists()){
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
                callback(list)
            }else{
                onError(errorMessage!!)
            }

        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }

    fun getOtherUserFollowers(userUid: String, onError: (String) -> Unit, callback: (Int) -> Unit){
        firebaseUtil.getOtherUserFollowers(userUid).get().addOnSuccessListener {
            val followers = it.size()
            callback(followers)

        }.addOnFailureListener {
            it.localizedMessage?.let{it1 -> onError(it1)}
        }
    }

    fun getOtherUsersFollowing(userUid: String, onError: (String) -> Unit, callback: (Int) -> Unit){
        firebaseUtil.getOtherUserFollowing(userUid).get().addOnSuccessListener {
            val following = it.size()
            callback(following)
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }

    fun getOtherUserBlock(userUid: String, onError: (String) -> Unit, onSuccess: (String) -> Unit){
        firebaseUtil.getOtherUserBlocks(userUid).document(firebaseUtil.currentUserId()).get().addOnSuccessListener {

            if (it.exists()){
                onSuccess(it.id)
            }
        }.addOnFailureListener {
            it.localizedMessage?.let{it1 -> onError(it1)}
        }
    }

    fun getOtherUserBackground(userUid: String, onError: (String) -> Unit, onSuccess: (String) -> Unit){
        firebaseUtil.getOtherUserBackground(userUid).document(userUid).get().addOnSuccessListener {
            if(it.exists()){
                val profileBackgroundImageUrl = it.getString("Background")
                if (profileBackgroundImageUrl != null) {
                    onSuccess(profileBackgroundImageUrl)
                }
            }
        }.addOnFailureListener {
            it.localizedMessage?.let {it1-> onError(it1) }
        }
    }
}