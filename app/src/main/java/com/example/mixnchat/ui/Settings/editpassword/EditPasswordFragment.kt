package com.example.mixnchat.ui.Settings.editpassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditPasswordBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
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

        if (currentPassword.isEmpty()){
            binding.currentPasswordEditText.error = "Field can't be empty"
        }else if(newPassword.isEmpty()){
            binding.newPasswordEditText.error = "Field can't be empty"
        }else{

            val authCredential = auth.currentUser!!.email?.let {
                EmailAuthProvider.getCredential(it,  currentPassword) }

            if (authCredential != null) {
                auth.currentUser!!.reauthenticate(authCredential).addOnSuccessListener {
                    auth.currentUser!!.updatePassword(newPassword).addOnSuccessListener {
                        androidUtil.showToast(requireContext(),"Password successfully updated!")
                        parentFragmentManager.popBackStack()
                    }.addOnFailureListener {
                        androidUtil.showToast(requireContext(),it.localizedMessage!!)
                    }
                }.addOnFailureListener {
                    androidUtil.showToast(requireContext(),it.localizedMessage!!)
                }
            }
        }
    }

}