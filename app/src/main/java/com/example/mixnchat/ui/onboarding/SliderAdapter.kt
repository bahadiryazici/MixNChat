package com.example.mixnchat.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mixnchat.data.Intro
import com.example.mixnchat.databinding.SlideLayoutBinding

class SliderAdapter(private val list: ArrayList<Intro>) : RecyclerView.Adapter<SliderAdapter.SlideViewHolder>() {
    class SlideViewHolder(private val slideBinding: SlideLayoutBinding) : RecyclerView.ViewHolder(slideBinding.root) {
        fun bind(item : Intro){
            with(slideBinding){
                slideHeading.text =  item.header
                slideDescription.text = item.description
                slideImage.setImageResource(item.image)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        return SlideViewHolder(SlideLayoutBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }
    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        holder.bind(list[position])
    }
    override fun getItemCount(): Int {
        return list.size
    }
}
