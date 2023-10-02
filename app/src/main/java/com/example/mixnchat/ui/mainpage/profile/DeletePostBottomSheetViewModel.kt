package com.example.mixnchat.ui.mainpage.profile

import androidx.lifecycle.ViewModel
import com.example.mixnchat.utils.FirebaseUtil

class DeletePostBottomSheetViewModel : ViewModel() {
    private val firebaseUtil = FirebaseUtil()
    fun deletePost(argPostId : String, onError : (String) -> Unit, onSuccess : () -> Unit){
        val imageName = "$argPostId.jpg"
        val imageReference =firebaseUtil.getPostsFromStorage().child(firebaseUtil.currentUserId()).child(imageName)
        imageReference.delete().addOnSuccessListener {
            firebaseUtil.getCurrentUserPosts().document(argPostId).delete().addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
               it.localizedMessage?.let { it1 -> onError(it1) }
            }
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
}