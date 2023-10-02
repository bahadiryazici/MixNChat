package com.example.mixnchat.ui.mainpage.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mixnchat.data.ChatMessageModel
import com.example.mixnchat.databinding.RecyclerChatBinding
import com.example.mixnchat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth


class ChatRecyclerAdapter(options: FirestoreRecyclerOptions<ChatMessageModel?>?, var firebaseUtil: FirebaseUtil) : FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>(options!!) {

    init {
        firebaseUtil = FirebaseUtil()
    }
    override fun onBindViewHolder(holder: ChatModelViewHolder, position: Int, model: ChatMessageModel) {
        if (model.senderId == FirebaseAuth.getInstance().currentUser!!.uid) {
            if(!model.seen!!){
                holder.binding.textSeen.text = "Delivered"
            }else{
                holder.binding.textSeen.text = "Seen"
            }

          holder.binding.leftChatLayout.visibility = View.GONE
          holder.binding.rightChatLayout.visibility = View.VISIBLE
          holder.binding.rightChatTextView.text = model.message
          holder.binding.rightChatHour.text = firebaseUtil.timeStampToString(model.timestamp!!)

        } else {

           holder.binding.rightChatLayout.visibility = View.GONE
           holder.binding.leftChatLayout.visibility = View.VISIBLE
           holder.binding.leftChatTextView.text = model.message
           holder.binding.leftChatHour.text = firebaseUtil.timeStampToString(model.timestamp!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val view = RecyclerChatBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ChatModelViewHolder(view)
    }
    inner class ChatModelViewHolder(var binding: RecyclerChatBinding) : RecyclerView.ViewHolder(binding.root) {}


}