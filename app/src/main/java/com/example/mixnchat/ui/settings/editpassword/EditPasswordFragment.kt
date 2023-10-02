package com.example.mixnchat.ui.settings.editpassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentEditPasswordBinding
import com.example.mixnchat.utils.AndroidUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



class EditPasswordFragment : Fragment() {

    private var _binding : FragmentEditPasswordBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private val androidUtil = AndroidUtil()
    private lateinit var viewModel: EditPasswordViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditPasswordBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        viewModel = ViewModelProvider(this)[EditPasswordViewModel::class.java]
        auth = Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button2.setOnClickListener {
            savePassword()
        }
        binding.imageView8.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    private fun savePassword() {
        val currentPassword = binding.currentPasswordEditText.text.toString()
        val newPassword = binding.newPasswordEditText.text.toString()
        val authCredential = auth.currentUser!!.email?.let { EmailAuthProvider.getCredential(it,  currentPassword) }
        if (currentPassword.isEmpty()){
            binding.currentPasswordEditText.error = this.getString(R.string.field_cant_be_empty)
        }else if(newPassword.isEmpty()){
            binding.newPasswordEditText.error = this.getString(R.string.field_cant_be_empty)
        }else{
            if (authCredential != null) {
                viewModel.updatePassword(newPassword,authCredential, onError = {}, onSuccess = {
                    androidUtil.showToast(requireContext(),this.getString(R.string.password_successfully_updated))
                    parentFragmentManager.popBackStack()
                })
            }
        }
    }
}