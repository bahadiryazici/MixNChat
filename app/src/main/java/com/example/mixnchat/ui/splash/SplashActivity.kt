package com.example.mixnchat.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mixnchat.MainActivity
import com.example.mixnchat.R
import com.example.mixnchat.ui.login.LoginActivity
import com.example.mixnchat.ui.onboarding.OnboardingActivity
import com.example.mixnchat.utils.Constants
import com.example.mixnchat.utils.PreferencesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity()  {

    private lateinit var  preferences : PreferencesProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        preferences = PreferencesProvider(context = this.applicationContext)

        CoroutineScope(Dispatchers.Main).launch{
            delay(3000)
            val intent = if (preferences.getBoolean(Constants.KEY_0NBOARDING)){
                Intent(this@SplashActivity, LoginActivity::class.java)
            }else{
                Intent(this@SplashActivity, OnboardingActivity::class.java)
            }
            startActivity(intent)
        }
    }
}
