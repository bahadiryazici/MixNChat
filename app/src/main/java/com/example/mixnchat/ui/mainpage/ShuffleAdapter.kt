package com.example.mixnchat.ui.mainpage

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.RecyclerShuffleBinding
import com.squareup.picasso.Picasso
import kotlin.random.Random

class ShuffleAdapter(private val userList : ArrayList<Users>) : RecyclerView.Adapter<ShuffleAdapter.ShuffleViewHolder>() {

    class ShuffleViewHolder(var recyclerShuffleBinding : RecyclerShuffleBinding) : RecyclerView.ViewHolder(recyclerShuffleBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuffleViewHolder {
        val recyclerShuffleBinding = RecyclerShuffleBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ShuffleViewHolder(recyclerShuffleBinding)
    }

    override fun onBindViewHolder(holder: ShuffleViewHolder, position: Int) {
            holder.recyclerShuffleBinding.username.text = userList[position].username
            holder.recyclerShuffleBinding.biography.text = userList[position].biography
            Picasso.get().load(userList[position].profileURL).into(holder.recyclerShuffleBinding.profilePhoto)

            holder.itemView.setOnClickListener {
                val action = ShuffleFragmentDirections.actionShuffleFragmentToReviewedProfilPage(userList[position].userUid!!)
                Navigation.findNavController(it).navigate(action)
            }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
