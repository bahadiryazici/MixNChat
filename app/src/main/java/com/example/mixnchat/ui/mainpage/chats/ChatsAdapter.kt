package com.example.mixnchat.ui.mainpage.chats

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.ChatRoomModel
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.RecyclerChatsBinding
import com.example.mixnchat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth

class ChatsAdapter(options: FirestoreRecyclerOptions<ChatRoomModel>, var firebaseUtil : FirebaseUtil, var context : Context) : FirestoreRecyclerAdapter<ChatRoomModel,ChatsAdapter.ViewHolder>(options) {

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

                val lastMessageSentByMe = model.lastMessageSenderId.equals(FirebaseAuth.getInstance().currentUser!!.uid)

                val otherUserModel = task.result.toObject(Users::class.java)
                if (otherUserModel != null) {
                    holder.binding.userName.text = otherUserModel.username
                }
                if (lastMessageSentByMe)
                    holder.binding.lastChat.text = context.getString(R.string.you) + model.lastMessage
                else
                    holder.binding.lastChat.text = model.lastMessage
                holder.binding.lastMessageTime.text = firebaseUtil.timeStampToString(model.lastMessageTimeStamp!!)
                Glide.with(holder.itemView.context).asBitmap().load(otherUserModel!!.profileUrl).into(holder.binding.profPhoto)

                holder.itemView.setOnClickListener {
                    val action =
                    otherUserModel.username?.let { it1 ->
                        otherUserModel.profileUrl?.let { it2 ->
                            otherUserModel.userUid?.let { it3 ->
                                ChatsFragmentDirections.actionChatsFragmentToChatFragment(
                                    it1, it2, it3
                                )
                            }
                        }
                    }
                    if (action != null) {
                        Navigation.findNavController(it).navigate(action)
                    }
                }

            }
        }
    }
}