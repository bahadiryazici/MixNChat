package com.example.mixnchat.ui.settings.editmail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentEditEmailBinding
import com.example.mixnchat.utils.AndroidUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class EditEmailFragment : Fragment() {

    private var _binding : FragmentEditEmailBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private val androidUtil = AndroidUtil()
    private lateinit var viewModel: EditEmailViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditEmailBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[EditEmailViewModel::class.java]
        viewModel.init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView8.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.button2.setOnClickListener {
            updateMail()
        }
    }

    private fun updateMail() {
        val currentMail = binding.currentMailEditText.text.toString()
        val newMail = binding.newMailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val authCredential = EmailAuthProvider.getCredential(currentMail,password)


        if (currentMail.isEmpty()){
            binding.currentMailEditText.error = this.getString(R.string.field_cant_be_empty)
        }else if(newMail.isEmpty()){
            binding.newMailEditText.error = this.getString(R.string.field_cant_be_empty)
        }else if (password.isEmpty()){
            binding.passwordEditText.error = this.getString(R.string.field_cant_be_empty)
        }else{
            viewModel.updateMail(
                newMail,
                authCredential,
                onSuccess = {
                    Toast.makeText(requireContext(),this.getString(R.string.email_successfully_updated), Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                },
                onError = {androidUtil.showToast(requireContext(),it)})
        }
    }
}
