package com.example.mixnchat.ui.mainpage.chats


import androidx.lifecycle.ViewModel
import com.example.mixnchat.data.Users
import com.example.mixnchat.utils.FirebaseUtil

class ChatsViewModel : ViewModel() {
    private val firebaseUtil = FirebaseUtil()
    fun getFollowingUsers(callback : (ArrayList<Users>) -> Unit, onError : (String) -> Unit){
        val followingList = ArrayList<Users>()
        firebaseUtil.getCurrentUserFollowing().addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { onError(it) }
            }
            if(value != null && !value.isEmpty){
                val documents = value.documents
                for(document in documents){
                    val username = document.getString("username")
                    val pp = document.getString("profileUrl")
                    val userUid = document.getString("userUid")
                    if(username != null && pp != null && userUid != null){
                        val user = Users(username,null,pp,userUid,null)
                        followingList.add(user)
                    }
                }
                callback(followingList)
            }
        }
    }
}