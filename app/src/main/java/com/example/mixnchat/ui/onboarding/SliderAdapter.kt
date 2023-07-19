package com.example.mixnchat.ui.onboarding

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mixnchat.MainActivity
import com.example.mixnchat.R
import com.example.mixnchat.utils.Constants
import com.example.mixnchat.utils.PreferencesProvider

class SliderAdapter(private val context: Context, private val viewPager: ViewPager2) : RecyclerView.Adapter<SliderAdapter.SlideViewHolder>() {
    private val slideImages = arrayOf(
        R.drawable.highfive,
        R.drawable.searching,
        R.drawable.discussion,
        R.drawable.security,
        R.drawable.humanresource
    )

    private val slideHeadings = arrayOf(
        "Create Your Profile",
        "Start Exploring",
        "Engaging Conversations",
        "Privacy and Security",
        "Make New Friends"
    )

    private val slideDescriptions = arrayOf(
        "The first step is to create a great profile that introduces yourself! Add your own photos, share your hobbies and interests. So other users can get to know you better and find common points.",
        "Our app offers random buddies for you. Start chatting with people who are right for you.",
        "If you can find interesting people, you can start a conversation and make new connections by messaging them. Explore personal chats based on your interests and personal preferences.",
        "We prioritize your privacy and security. You can only communicate with people you allow. We don't allow offensive behavior in our app, and we encourage everyone to communicate respectfully with our community guidelines.",
        "Our app gives you the chance to meet new people and make meaningful connections. Start socializing in our app today to add new friends and valuable connections to your life.\n" +
                "\n" +
                "To get started, please create an account and join an exciting social experience!"
    )

    private val preferences = PreferencesProvider(context)

    private fun goToNextSlide() {
        val currentItem = viewPager.currentItem
        if (currentItem < slideImages.size - 1) {
            viewPager.currentItem = currentItem + 1
        } else {
            preferences.putBoolean(Constants.KEY_0NBOARDING,true)
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)

        }
    }

    class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val slideImageView: ImageView = itemView.findViewById(R.id.slideImage)
        val slideHeadingView: TextView = itemView.findViewById(R.id.slideHeading)
        val slideDescriptionView: TextView = itemView.findViewById(R.id.slideDescription)
        val nextButton: Button = itemView.findViewById(R.id.nextButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.slide_layout, parent, false)
        return SlideViewHolder(layout)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {

        holder.slideImageView.setImageResource(slideImages[position])
        holder.slideHeadingView.text = slideHeadings[position]
        holder.slideDescriptionView.text = slideDescriptions[position]

        holder.nextButton.setOnClickListener {
            goToNextSlide()
        }

    }

    override fun getItemCount(): Int {
        return slideImages.size
    }
}
