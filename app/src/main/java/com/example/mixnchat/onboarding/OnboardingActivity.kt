package com.example.mixnchat.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.mixnchat.databinding.ActivityOnboardingBinding
import com.example.mixnchat.onboarding.screens.FifthScreen
import com.example.mixnchat.onboarding.screens.FirstScreen
import com.example.mixnchat.onboarding.screens.FourthScreen
import com.example.mixnchat.onboarding.screens.SecondScreen
import com.example.mixnchat.onboarding.screens.SixthScreen
import com.example.mixnchat.onboarding.screens.ThirdScreen

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)


        val fragmentList = arrayListOf<Fragment>(
            FirstScreen(),
            SecondScreen(),
            ThirdScreen(),
            FourthScreen(),
            FifthScreen(),
            SixthScreen(),
        )

        val adapter = ViewPagerAdapter(fragmentList,this.supportFragmentManager,lifecycle)
        binding.viewPager.adapter = adapter
        supportActionBar?.hide()
    }
}