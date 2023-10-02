package com.example.mixnchat.ui.mainpage.chats

import androidx.lifecycle.ViewModel
import com.example.mixnchat.data.ChatMessageModel
import com.example.mixnchat.data.ChatRoomModel
import com.example.mixnchat.utils.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration

class ChatViewModel : ViewModel() {
    private val firebaseUtil = FirebaseUtil()
    private var chatRoomModel: ChatRoomModel ?= null

    fun getChatRoomReference(chatRoomId : String,  userUid : String ){
        firebaseUtil.allChatRoomCollectionReference().document(chatRoomId).get().addOnCompleteListener {task ->
            if (task.isSuccessful){
                chatRoomModel = task.result.toObject(ChatRoomModel::class.java)
                if (chatRoomModel == null){
                    //first  time chat
                    chatRoomModel = ChatRoomModel(
                        chatRoomId,
                        listOf(firebaseUtil.currentUserId(), userUid) as List<String>,
                        Timestamp.now(),
                        "",
                        ""
                    )
                    firebaseUtil.allChatRoomCollectionReference().document(chatRoomId).set(chatRoomModel!!)
                }
            }
        }
    }

    fun sendMessageToUser(message : String, chatRoomId: String, userUid: String, onSuccess : () -> Unit){
        chatRoomModel?.lastMessageTimeStamp = Timestamp.now()
        chatRoomModel?.lastMessageSenderId = firebaseUtil.currentUserId()
        chatRoomModel?.lastMessage = message
        firebaseUtil.allChatRoomCollectionReference().document(chatRoomId).set(chatRoomModel!!)
        val chatMessageModel = ChatMessageModel(message,
            firebaseUtil.currentUserId(),
            Timestamp.now(),
            false,
            userUid)
        firebaseUtil.getRoom(chatRoomId).collection("chats")
            .add(chatMessageModel).addOnCompleteListener {
                if (it.isSuccessful){
                    onSuccess()
                }
            }
    }

    fun seenMessage(chatRoomId: String, callBack : (ListenerRegistration) -> Unit){
        val chatCollection = firebaseUtil.getRoom(chatRoomId).collection("chats")
        callBack( chatCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshots != null) {
                for (snapshot in snapshots) {
                    val chat = snapshot.toObject(ChatMessageModel::class.java)
                    if (chat.receiverId == firebaseUtil.currentUserId() && !chat.seen!!) {
                        val chatRef = chatCollection.document(snapshot.id)
                        val updateData = hashMapOf(
                            "seen" to true
                        )
                        chatRef.update(updateData as Map<String, Any>)
                    }
                }
            }
        })
    }
}