package com.example.mixnchat.ui.login.signup

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mixnchat.R
import com.example.mixnchat.ui.mainpage.MainActivity
import com.example.mixnchat.databinding.FragmentRegisterBinding
import com.example.mixnchat.utils.AndroidUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.IOException



class RegisterFragment : Fragment() {

    private var _binding : FragmentRegisterBinding?= null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedPicture : Uri? = null
    private var selectedBitmap : Bitmap? = null
    private val androidUtil = AndroidUtil()
    private lateinit var viewModel : RegisterViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        auth= Firebase.auth
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        viewModel.init()
        registerLauncher()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signupButton.setOnClickListener {
            signUp()
        }
        binding.pp.setOnClickListener{
            ppClicked()
        }
        binding.datePickerButton.setOnClickListener {
            androidUtil.initDatePicker(requireContext(),binding.datePickerButton).show()
        }
    }

    private fun signUp(){
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val username = binding.usernameEditText.text.toString()
        val country = binding.countrySpinner.selectedItem.toString()
        val biography = binding.biographyEditText.text.toString()
        val gender = binding.genderSpinner.selectedItem.toString()
        val date = binding.datePickerButton.text.toString()


        viewModel.errorMessagePicture = getString(R.string.selectPictureMessage)
        viewModel.errorMessageAll = getString(R.string.allFieldsMessage)
        viewModel.signUp(email,password,username,country,biography,gender,date,selectedPicture,
        onSuccess ={
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        },
        onError = {errorMessage ->
            androidUtil.showToast(requireContext(),errorMessage)
        })
    }

    private fun ppClicked(){
        androidUtil.askPermission(requireContext(),requireActivity(),requireView(), permissionLauncher, activityResultLauncher)
    }
    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                    if(selectedPicture!= null){
                        binding.miniCamera.visibility = View.GONE
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    selectedPicture!!
                                )
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.pp.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    selectedPicture
                                )
                                binding.pp.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                androidUtil.showToast(requireContext(),this.getString(R.string.permissionNeedMessage))
            }
        }
    }
}