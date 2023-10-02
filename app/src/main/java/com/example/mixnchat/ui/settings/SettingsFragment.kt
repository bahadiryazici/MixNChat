package com.example.mixnchat.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mixnchat.databinding.FragmentSettingsBinding
import com.example.mixnchat.ui.login.LoginActivity
import com.example.mixnchat.ui.mainpage.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class SettingsFragment : Fragment() {

    private var _binding : FragmentSettingsBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        _binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        init()
        return binding.root
    }
    private fun init() {
        auth = Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editProfileText.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToEditProfileFragment())
        }
        binding.editEmailText.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToEditEmailFragment())
        }
        binding.editPasswordText.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToEditPasswordFragment())
        }
        binding.helpSupportText.setOnClickListener {

        }
        binding.termsPoliciesText.setOnClickListener {

        }
        binding.reportProblemText.setOnClickListener {

        }
        binding.logOutText.setOnClickListener {
            logOut()
        }
        binding.goBackImage.setOnClickListener {
            goBack()
        }
    }

    private fun goBack() {
        val intent = Intent(requireActivity(),MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
    private fun logOut(){
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {task ->
            if (task.isSuccessful){
                auth.signOut()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }
}