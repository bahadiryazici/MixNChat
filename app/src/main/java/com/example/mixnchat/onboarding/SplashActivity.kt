package com.example.mixnchat.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.mixnchat.MainActivity


class SplashActivity : AppCompatActivity()  {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler().postDelayed({
            if (onBoardingFinished()){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, OnboardingActivity::class.java)
                startActivity(intent)
                finish()
            }

        },3000)

    }


    private fun  onBoardingFinished() : Boolean{
        val sp = this.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sp.getBoolean("Finished", false)
    }
}
