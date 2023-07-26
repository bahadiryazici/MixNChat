package com.example.mixnchat.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mixnchat.MainActivity
import com.example.mixnchat.databinding.FragmentOTPBinding
import com.example.mixnchat.databinding.FragmentPhoneBinding
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


class PhoneFragment : Fragment() {

    private var _binding : FragmentPhoneBinding ?= null
    private val binding get() = _binding!!
    private lateinit var fadeIn : AlphaAnimation
    private lateinit var fadeOut : AlphaAnimation
    private lateinit var animSet : AnimationSet
    private lateinit var auth : FirebaseAuth
    var phone : String ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneBinding.inflate(layoutInflater,container, false)
        init()
        return binding.root
    }

    private fun init() {

        auth = Firebase.auth
        if (auth.currentUser!=null){
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        animationFirst()

        with(binding){
            iconView.animation = animSet
            textView10.animation = animSet
            textView11.animation = animSet
            textView13.animation = animSet

            iconView.visibility = View.INVISIBLE
            textView10.visibility = View.INVISIBLE
            textView11.visibility = View.INVISIBLE
            textView13.visibility = View.INVISIBLE
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            animationSecond()
            with(binding){
                secondDescription.animation = animSet
                secondHeader.animation = animSet
                button2.animation = animSet
                editTextText.animation = animSet

                secondDescription.visibility = View.VISIBLE
                secondHeader.visibility = View.VISIBLE
                button2.visibility = View.VISIBLE
                editTextText.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button2.setOnClickListener {
            loginWithPhone()
        }
    }

    private fun loginWithPhone() {
       phone = binding.editTextText.text.toString()

        if(phone!!.isNotEmpty()){
            if (phone!!.length == 10){
                phone = "+90$phone"
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone!!)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }else{
                binding.editTextText.error = "Please Enter Correct Number"
            }
        }else{
            binding.editTextText.error = "Please Enter Number"
        }
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


            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
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

            val action = PhoneFragmentDirections.actionPhoneFragmentToOTPFragment(verificationId,token,phone!!)
            findNavController().navigate(action)

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