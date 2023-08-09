package com.example.mixnchat.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat


private var auth : FirebaseAuth = Firebase.auth
private var storage : FirebaseStorage = Firebase.storage
private var firestore : FirebaseFirestore = Firebase.firestore
class FirebaseUtil {

    fun currentUserId() : String {
        return auth.currentUser!!.uid
    }

    fun allChatRoomCollectionReference() : CollectionReference {
        return  firestore.collection("chatrooms")
    }

    fun getOtherUserFromChatRoom(userIds : List<String>) : DocumentReference {
        return if (userIds[0] == auth.currentUser!!.uid){
            getAllUser().document(userIds[1])
        }else{
            getAllUser().document(userIds[0])
        }
    }

     fun getAllUser() : CollectionReference{
        return  firestore.collection("Users")
    }

    fun timeStampToString(timeStamp: com.google.firebase.Timestamp): String {
        return SimpleDateFormat("HH:MM").format(timeStamp.toDate())
    }


    fun getRoom ( chatRoomId: String) : DocumentReference{
        return firestore.collection("chatrooms").document(chatRoomId)
    }

    fun getChatRoomId(userId1 : String, userId2: String) :String{
        if (userId1.hashCode() < userId2.hashCode()){
            return userId1+"_"+userId2
        }else{
            return userId2+"_"+userId1
        }
    }

    fun getCurrentUserFollowing() : CollectionReference{
        return firestore.collection(auth.currentUser!!.uid + "Following")
    }

    fun getCurrentUserPosts() : CollectionReference{
        return firestore.collection(auth.currentUser!!.uid + "Posts")
    }

    fun getCurrentUserFollowers() : CollectionReference{
        return  firestore.collection(auth.currentUser!!.uid + "Followers")
    }

    fun getCurrentUserBackground() : CollectionReference{
        return  firestore.collection(auth.currentUser!!.uid + "Background")
    }

    fun getCurrentUserBlocks() : CollectionReference{
        return  firestore.collection(auth.currentUser!!.uid + "Blocks")
    }

    fun getBackgroundPhotosFromStorage() : StorageReference{
        return   storage.reference.child("BackgroundPhotos")
    }
    fun getPostsFromStorage() : StorageReference{
        return   storage.reference.child("Posts")
    }

    fun getProfilePhotoFromStorage() : StorageReference{
        return  storage.reference.child("profilePhotos")
    }

    fun getOtherUserPosts(userUid : String ) : CollectionReference{
        return firestore.collection(userUid + "Posts")
    }

    fun getOtherUserFollowers(userUid : String) : CollectionReference{
        return firestore.collection(userUid + "Followers")
    }
    fun getOtherUserFollowing(userUid : String) : CollectionReference{
        return firestore.collection(userUid + "Following")
    }
    fun getOtherUserBlocks(userUid: String) : CollectionReference{
        return firestore.collection(userUid + "Blocks")
    }
    fun getOtherUserBackground(userUid: String) : CollectionReference{
        return firestore.collection(userUid + "Background")
    }

}


