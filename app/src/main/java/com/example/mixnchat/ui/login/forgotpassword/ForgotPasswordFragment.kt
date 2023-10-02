package com.example.mixnchat.ui.login.forgotpassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mixnchat.databinding.FragmentForgotPasswordBinding
import com.example.mixnchat.utils.AndroidUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ForgotPasswordFragment : Fragment() {

    private var _binding : FragmentForgotPasswordBinding?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private val action = ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToLoginFragment()
    private val androidUtil = AndroidUtil()
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPasswordBinding.inflate(layoutInflater,container,false)
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        viewModel.init()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToLoginText.setOnClickListener {
            findNavController().navigate(action)
        }
        binding.submitButton.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = binding.mailEdittext.text.toString()
     viewModel.resetPassword(email, onSuccess = {
         findNavController().navigate(action)
     }, onError = {
         androidUtil.showToast(requireContext(),it)
     })
    }
}