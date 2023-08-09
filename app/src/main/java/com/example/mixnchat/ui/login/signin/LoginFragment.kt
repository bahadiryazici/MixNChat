package com.example.mixnchat.ui.login.signin



import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mixnchat.R
import com.example.mixnchat.ui.mainpage.MainActivity
import com.example.mixnchat.databinding.FragmentLoginBinding
import com.example.mixnchat.utils.AndroidUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class LoginFragment : Fragment() {

    private var _binding : FragmentLoginBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val androidUtil = AndroidUtil()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        firestore = Firebase.firestore
        auth = Firebase.auth
        storage = Firebase.storage
        val currentUser = auth.currentUser
        if(currentUser != null){
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.forgetPassword.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
            findNavController().navigate(action)
        }
        binding.signUpText.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }

        binding.loginButton.setOnClickListener {
            login()
        }
    }
    private fun login() {
        val email = binding.mailEdit.text.toString()
        val password = binding.passwordEdit.text.toString()
        if(email == "" || password == ""){
            binding.mailEdit.error = this.getString(R.string.fillFieldMessage)
            binding.passwordEdit.error =  this.getString(R.string.fillFieldMessage)
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                    if (auth.currentUser!!.isEmailVerified){
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }else{
                        androidUtil.showToast(requireContext(),this.getString(R.string.verifyMailMessage))
                    }
            }.addOnFailureListener {
                    androidUtil.showToast(requireContext(),it.localizedMessage!!)
            }
        }
    }
}