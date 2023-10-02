package com.example.mixnchat.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mixnchat.R
import com.example.mixnchat.data.Intro
import com.example.mixnchat.databinding.ActivityOnboardingBinding
import com.example.mixnchat.ui.login.LoginActivity
import com.example.mixnchat.utils.Constants
import com.example.mixnchat.utils.PreferencesProvider


class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)
        init()
    }
    private fun init(){

        val sliderAdapter = SliderAdapter(arrayListOf(
            Intro(this.getString(R.string.sliderHeader1),this.getString(R.string.sliderDescription1),R.drawable.highfive),
            Intro(this.getString(R.string.sliderHeader2),this.getString(R.string.sliderDescription2),R.drawable.searching),
            Intro(this.getString(R.string.sliderHeader3),this.getString(R.string.sliderDescription3),R.drawable.discussion),
            Intro(this.getString(R.string.sliderHeader4),this.getString(R.string.sliderDescription4),R.drawable.security),
            Intro(this.getString(R.string.sliderHeader5),this.getString(R.string.sliderDescription5),R.drawable.humanresource)
        ))
        binding.viewPager.adapter = sliderAdapter
        supportActionBar?.hide()
        binding.dots.attachTo(binding.viewPager)
        binding.button.setOnClickListener {
            goToNextSlide(sliderAdapter)
        }
        binding.viewPager.isUserInputEnabled = false
        binding.backButton.setOnClickListener {
            goBackSlide(sliderAdapter)
        }
        binding.backButton.visibility = View.INVISIBLE
    }
    private fun goToNextSlide(slideradapter : SliderAdapter) {
        val preferences = PreferencesProvider(this)
        val  currentItem = binding.viewPager.currentItem
        if (currentItem < slideradapter.itemCount - 1 ) {
            binding.viewPager.currentItem = currentItem + 1
            binding.backButton.visibility = View.VISIBLE
            if(currentItem == slideradapter.itemCount - 2){
                binding.button.text = this.getString(R.string.finish)
            }
        }
        else {
            preferences.putBoolean(Constants.KEY_0NBOARDING,true)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun goBackSlide(slideradapter: SliderAdapter){
        val  currentItem = binding.viewPager.currentItem
        binding.viewPager.currentItem = currentItem - 1
        if (currentItem == 1){
            binding.backButton.visibility = View.INVISIBLE
        }
        if(currentItem < slideradapter.itemCount){
            binding.button.text =  this.getString(R.string.next)
        }
    }
}