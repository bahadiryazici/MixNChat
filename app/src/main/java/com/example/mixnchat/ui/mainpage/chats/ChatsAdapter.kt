package com.example.mixnchat.ui.mainpage.chats

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.ChatRoomModel
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.RecyclerChatsBinding
import com.example.mixnchat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ChatsAdapter(options: FirestoreRecyclerOptions<ChatRoomModel>, var firebaseUtil : FirebaseUtil, var context : Context)
    : FirestoreRecyclerAdapter<ChatRoomModel,ChatsAdapter.ViewHolder>(options) {
    init {
        firebaseUtil = FirebaseUtil()
    }
    class ViewHolder(val binding : RecyclerChatsBinding) : RecyclerView.ViewHolder(binding.root){}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerChatsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ChatRoomModel) {
        firebaseUtil.getOtherUserFromChatRoom(model.userIds).get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val otherUserModel = task.result.toObject(Users::class.java)
                if (otherUserModel != null) {
                    holder.binding.userName.text = otherUserModel.username
                    val userName = hashMapOf<String,Any>()
                    userName["username"] = otherUserModel.username!!
                    firebaseUtil.getUsernameOtherSpokenUsers().document(otherUserModel.username)
                        .set(userName).addOnSuccessListener {}
                }
                if (model.lastMessageSenderId == firebaseUtil.currentUserId()){
                    holder.binding.lastChat.text = context.getString(R.string.you) + model.lastMessage
                }else{
                    holder.binding.lastChat.text = model.lastMessage
                }
                holder.binding.lastMessageTime.text = firebaseUtil.timeStampToString(model.lastMessageTimeStamp!!)

                if(otherUserModel != null){
                    Glide.with(context).asBitmap().load(otherUserModel.profileUrl).into(holder.binding.profPhoto)
                }
                holder.itemView.setOnClickListener {
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("username",otherUserModel!!.username)
                    intent.putExtra("profileUrl",otherUserModel.profileUrl)
                    intent.putExtra("userId",otherUserModel.userUid)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }
}