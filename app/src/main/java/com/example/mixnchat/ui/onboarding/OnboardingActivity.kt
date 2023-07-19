package com.example.mixnchat.ui.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mixnchat.databinding.ActivityOnboardingBinding


class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)


        val sliderAdapter = SliderAdapter(this,binding.viewPager)
        binding.viewPager.adapter = sliderAdapter
        supportActionBar?.hide()
    }
}