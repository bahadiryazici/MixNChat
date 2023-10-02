package com.example.mixnchat.ui.login.editprofile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentEditProfileBinding
import com.example.mixnchat.utils.AndroidUtil
import java.io.IOException


class EditProfileFragment : Fragment() {

    private var _binding : FragmentEditProfileBinding ?= null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedPicture : Uri? = null
    private var selectedBitmap : Bitmap? = null
    private val androidUtil = AndroidUtil()
    private var ppUrl : String ?= null
    private lateinit var viewModel: EditProfileViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
        getProfile()
        registerLauncher()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backImage.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.saveChangesButton.setOnClickListener {
            uploadUser()
        }
        binding.editProfilePhotoImageView.setOnClickListener {
            setPhoto()
        }
        binding.birthEditText.setOnClickListener {
            androidUtil.initDatePicker(requireContext(),binding.birthEditText)
        }
    }

    private fun setPhoto() {
         androidUtil.askPermission(requireContext(),requireActivity(),requireView(),permissionLauncher, activityResultLauncher)
    }

    private fun getProfile(){
        androidUtil.startAnimation(binding.animationView,binding.scrollView)
        viewModel.getProfile( {
            with(binding){
                Glide.with(requireContext()).load(it[0]).into(editProfilePhotoImageView)
                bioEditText.setText(it[1])
                countrySpinner.setSelection(getIndex(countrySpinner,it[2]))
                genderSpinner.setSelection(getIndex(genderSpinner,it[3]))
                nameEditText.setText(it[4])
                birthEditText.text = it[5]
            }
        }, onError = {
            androidUtil.showToast(requireContext(), it)
        }, callBack = {
            ppUrl = it
        })
        androidUtil.stopAnimation(binding.animationView,binding.scrollView)
    }
    private fun getIndex(spinner: Spinner, s: String): Int {
        for(i in 0 until spinner.count){
            if (spinner.getItemAtPosition(i).toString() == s){
                return i
            }
        }
        return 0
    }
    private fun uploadUser() {
        if (selectedPicture != null) {
            // if new photo chosen
            viewModel.uploadUser(
                onSuccess = {
                viewModel.uploadNewProfilePhoto(
                    selectedPicture!!,
                    onSuccess = { updateUserWithUri(it)},
                    onError = { androidUtil.showToast(requireContext(), it) }
                )
                },{
                androidUtil.showToast(requireContext(),it)
            })
        } else {
            // if its old photo, just update others
            val ppUri = Uri.parse(ppUrl!!)
            updateUserWithUri(ppUri.toString())
        }
    }

    private fun updateUserWithUri(ppUri: String) {
        val username = binding.nameEditText.text.toString()
        val country = binding.countrySpinner.selectedItem.toString()
        val gender = binding.genderSpinner.selectedItem.toString()
        val biography = binding.bioEditText.text.toString()
        val date = binding.birthEditText.text.toString()
        viewModel.updateUserWithUri(ppUri,username, country, gender, biography, date, onSuccess = {
            androidUtil.showToast(requireContext(), this.getString(R.string.profilUpdateMessage))
            parentFragmentManager.popBackStack()
        }, onError = {
            androidUtil.showToast(requireContext(),it)
        })
    }
    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data

                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.editProfilePhotoImageView.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPicture)
                            binding.editProfilePhotoImageView.setImageBitmap(selectedBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
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
                Toast.makeText(requireActivity(), "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }
}