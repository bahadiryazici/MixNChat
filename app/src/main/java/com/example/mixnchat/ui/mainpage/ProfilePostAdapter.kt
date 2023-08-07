package com.example.mixnchat.ui.mainpage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mixnchat.data.Posts
import com.example.mixnchat.databinding.RecyclerProfilePhotosBinding
import com.example.mixnchat.utils.OnPostItemClickListener
import com.squareup.picasso.Picasso

class ProfilePostAdapter(private val postList: ArrayList<Posts>, private val onItemClickListener: OnPostItemClickListener) : RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder>() {
        class PostViewHolder(val postBinding : RecyclerProfilePhotosBinding) : RecyclerView.ViewHolder(postBinding.root) {
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val profilePostBinding = RecyclerProfilePhotosBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostViewHolder(profilePostBinding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        Picasso.get().load(postList[position].posts).into(holder.postBinding.imageView10)

        holder.itemView.setOnClickListener {
            val postUid =  postList[position].postUid
            if (postUid != null) {
                onItemClickListener.onPostItemClick(postUid)
            }
        }
    }
}
