package com.example.mixnchat.ui.mainpage.shuffle


import androidx.lifecycle.ViewModel
import com.example.mixnchat.data.Users
import com.example.mixnchat.utils.FirebaseUtil
import java.util.Random

class ShuffleViewModel : ViewModel() {

    private val firebaseUtil = FirebaseUtil()
    private val userArrayList = ArrayList<Users>()
    private val resultList = ArrayList<Users>()

    fun getUserByRandom(
        onError : (String) -> Unit,
        callBack : (ArrayList<Users>) -> Unit
    ){
        firebaseUtil.getAllUser().addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.localizedMessage?.let { onError(it) }
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val documents = snapshot.documents
                val allUsers = ArrayList<Users>()
                for (document in documents) {
                    val userName = document.getString("username")
                    val biography = document.getString("biography")
                    val profileUrl = document.getString("profileUrl")
                    val userUid = document.getString("userUid")
                    if (userName != null && biography != null && profileUrl != null) {
                        val user = Users(userName, biography, profileUrl, userUid)
                        allUsers.add(user)
                    }
                }
                val random = Random()
                while (userArrayList.size < 5 && userArrayList.size < allUsers.size) {
                    val randomIndex = random.nextInt(allUsers.size)
                    val randomUser = allUsers[randomIndex]
                    if (!userArrayList.contains(randomUser)) {
                        userArrayList.add(randomUser)
                    }
                }
                callBack(userArrayList)
            }
        }
    }

    fun getUser(onError: (String) -> Unit, callBack: (ArrayList<Users>) -> Unit){
        firebaseUtil.getAllUser().get().addOnSuccessListener {
            resultList.clear()
            for (document in it.documents) {
                val userName = document.getString("username")
                val biography = document.getString("biography")
                val profileUrl = document.getString("profileUrl")
                val userUid = document.getString("userUid")
                if (userName != null && biography != null && profileUrl != null) {
                    val user = Users(userName, biography, profileUrl, userUid)
                    resultList.add(user)
                }
            }
            callBack(resultList)
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> onError(it1) }
        }
    }
}