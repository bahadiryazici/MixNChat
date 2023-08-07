package com.example.mixnchat.ui.login.otp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.widget.Toast
import com.example.mixnchat.ui.mainpage.MainActivity
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentOTPBinding
import com.example.mixnchat.ui.login.OTPFragmentArgs
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class OTPFragment : Fragment() {
    private var _binding : FragmentOTPBinding ?= null
    private val  binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var OTP : String
    private lateinit var resendingToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var phone : String
    private lateinit var fadeIn : AlphaAnimation
    private lateinit var fadeOut : AlphaAnimation
    private lateinit var animSet : AnimationSet



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentOTPBinding.inflate(layoutInflater,container, false)
        init()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        arguments?.let {
            resendingToken = OTPFragmentArgs.fromBundle(it).resendToken
            OTP = OTPFragmentArgs.fromBundle(it).OTP
            phone = OTPFragmentArgs.fromBundle(it).phoneNumber
        }
        auth = Firebase.auth
        addTextChangeListener()

        animationFirst()

        with(binding){
            iconView2.animation = animSet
            textView17.animation = animSet
            textView18.animation = animSet
            textView19.animation = animSet

            iconView2.visibility = View.INVISIBLE
            textView17.visibility = View.INVISIBLE
            textView18.visibility = View.INVISIBLE
            textView19.visibility = View.INVISIBLE
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            animationSecond()
            with(binding){
                box1.animation = animSet
                box2.animation = animSet
                box3.animation = animSet
                box4.animation = animSet
                box5.animation = animSet
                box6.animation = animSet
                textView14.animation = animSet
                textView15.animation = animSet
                continueButton.animation = animSet
                resendPassword.animation = animSet
                textView15.text = requireActivity().getString(R.string.verify) + phone

                box1.visibility = View.VISIBLE
                box2.visibility = View.VISIBLE
                box3.visibility = View.VISIBLE
                box4.visibility = View.VISIBLE
                box5.visibility = View.VISIBLE
                box6.visibility = View.VISIBLE
                continueButton.visibility = View.VISIBLE
                resendPassword.visibility = View.VISIBLE
                textView14.visibility = View.VISIBLE
                textView15.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.continueButton.setOnClickListener {
            verifyOTP()
        }
        binding.resendPassword.setOnClickListener {
            resendVerificationCode()
        }
    }

     private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .setForceResendingToken(resendingToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    // Invalid request
                    Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                }

                is FirebaseTooManyRequestsException -> {
                    // The SMS quota for the project has been exceeded
                    Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                }

                is FirebaseAuthMissingActivityForRecaptchaException -> {
                    // reCAPTCHA verification attempted with null Activity
                    Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                }
            }
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            OTP = verificationId
            resendingToken = token
        }
    }

    private fun verifyOTP(){
        val typedOTP = with(binding){
            box1.text.toString() + box2.text.toString() + box3.text.toString() + box4.text.toString() + box5.text.toString() + box6.text.toString()
        }
        if(typedOTP.isNotEmpty()){
            if(typedOTP.length == 6){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(OTP,typedOTP)
                signInWithPhoneAuthCredential(credential)

            }else{
                Toast.makeText(requireContext(),"Please Enter Correct OTP", Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(requireContext(),"Please Enter OTP", Toast.LENGTH_LONG).show()
        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(requireContext(),"Authenticate Succesfull", Toast.LENGTH_LONG).show()
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
    private fun addTextChangeListener(){
        binding.box1.addTextChangedListener(EditTextWatcher(binding.box1))
        binding.box2.addTextChangedListener(EditTextWatcher(binding.box2))
        binding.box3.addTextChangedListener(EditTextWatcher(binding.box3))
        binding.box4.addTextChangedListener(EditTextWatcher(binding.box4))
        binding.box5.addTextChangedListener(EditTextWatcher(binding.box5))
        binding.box6.addTextChangedListener(EditTextWatcher(binding.box6))
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            when(view.id){
                R.id.box1 -> if (text.length == 1) binding.box2.requestFocus()
                R.id.box2 -> if (text.length == 1) binding.box3.requestFocus() else if(text.isEmpty()) binding.box1.requestFocus()
                R.id.box3 -> if (text.length == 1) binding.box4.requestFocus() else if(text.isEmpty()) binding.box2.requestFocus()
                R.id.box4 -> if (text.length == 1) binding.box5.requestFocus() else if(text.isEmpty()) binding.box3.requestFocus()
                R.id.box5 -> if (text.length == 1) binding.box6.requestFocus() else if(text.isEmpty()) binding.box4.requestFocus()
                R.id.box6 -> if (text.isEmpty()) binding.box5.requestFocus()
            }
        }
    }

    private fun animationFirst(){
        fadeIn = AlphaAnimation(0f,1f).apply {
            duration = 3000L
        }
        fadeOut = AlphaAnimation(1f,0f).apply {
            duration = 5000L
        }
        animSet = AnimationSet(false)
        animSet.addAnimation(fadeIn)
        animSet.addAnimation(fadeOut)
        animSet.startNow()
    }
    private fun animationSecond(){
        fadeIn = AlphaAnimation(0f,1f).apply {
            duration = 3000L
        }
        animSet = AnimationSet(false)
        animSet.addAnimation(fadeIn)
        animSet.startNow()
    }
}