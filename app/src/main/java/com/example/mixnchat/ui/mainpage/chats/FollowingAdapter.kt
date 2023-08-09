package com.example.mixnchat.ui.mainpage.chats

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixnchat.R

import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.RecyclerFollowingBinding

class FollowingAdapter( private val followingList : ArrayList<Users>) : RecyclerView.Adapter<FollowingAdapter.ViewHolder>() {

    inner class ViewHolder(val itemBinding : RecyclerFollowingBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerFollowingBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if(followingList.isEmpty()){
            holder.itemBinding.imageView11.setImageResource(R.drawable.edit_profile_icon)
            holder.itemBinding.imageView11.circleBackgroundColor = Color.LTGRAY
            holder.itemBinding.textView34.text = ""
        }else{
            holder.itemBinding.textView34.text = followingList[position].username
            Glide.with(holder.itemView.context).asBitmap().load(followingList[position].profileUrl).into(holder.itemBinding.imageView11)
            holder.itemView.setOnClickListener {
                val action = ChatsFragmentDirections.actionChatsFragmentToReviewedProfilPage(followingList[position].userUid!!)
                Navigation.findNavController(it).navigate(action)
            }
        }

    }

    override fun getItemCount(): Int {
        return if(followingList.isEmpty()){
            1
        }else{
            followingList.size
        }
    }

}