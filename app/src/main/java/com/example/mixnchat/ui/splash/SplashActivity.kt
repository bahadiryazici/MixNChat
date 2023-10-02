package com.example.mixnchat.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mixnchat.R
import com.example.mixnchat.data.Users
import com.example.mixnchat.ui.login.LoginActivity
import com.example.mixnchat.ui.mainpage.chats.ChatActivity
import com.example.mixnchat.ui.onboarding.OnboardingActivity
import com.example.mixnchat.utils.Constants
import com.example.mixnchat.utils.FirebaseUtil
import com.example.mixnchat.utils.PreferencesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity()  {

    private lateinit var  preferences : PreferencesProvider
    private val firebaseUtil = FirebaseUtil()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preferences = PreferencesProvider(context = this.applicationContext)
        if (firebaseUtil.isLoggedIn() && intent.extras != null){
            val userId = intent.extras!!.getString("userId")
            firebaseUtil.getAllUser().document(userId!!).get().addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val user = task.result.toObject(Users::class.java)
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("username", user!!.username)
                    intent.putExtra("userId", user.userUid)
                    intent.putExtra("profileUrl", user.profileUrl)
                 //   intent.putExtra("token", user.token)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }else{
            CoroutineScope(Dispatchers.Main).launch{
                delay(3000)
                val intent = if (preferences.getBoolean(Constants.KEY_0NBOARDING)){
                    Intent(this@SplashActivity, LoginActivity::class.java)

                }else{
                    Intent(this@SplashActivity, OnboardingActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
        }
        }
    }
