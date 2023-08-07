package com.example.mixnchat.ui.Settings.editprofile

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.IOException


class EditProfileFragment : Fragment() {

    private var _binding : FragmentEditProfileBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedPicture : Uri? = null
    private var selectedBitmap : Bitmap? = null
    private var phone : Any ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        auth = Firebase.auth
        storage = Firebase.storage
        firestore = Firebase.firestore
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
    }

    private fun setPhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(requireView(), requireActivity().getString(R.string.permission), Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"
                ) {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            } else {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)

        }

    }

    private fun getProfile(){
        with(binding){
            animationView.playAnimation()
            scrollView.visibility = View.INVISIBLE
        }
        firestore.collection("Users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
            if (it.exists()){
             val ppUrl = it.getString("profileUrl")
             val biography = it.getString("biography")
             val country = it.getString("country")
             val gender = it.getString("gender")
             val username = it.getString("username")
             phone = it.getString("phone").toString()
             with(binding){
                 Glide.with(requireContext()).load(ppUrl).into(editProfilePhotoImageView)
                 bioEditText.setText(biography.toString())
                 countrySpinner.setSelection(getIndex(countrySpinner,country.toString()))
                 genderSpinner.setSelection(getIndex(genderSpinner,gender.toString()))
                 nameEditText.setText(username.toString())
             }
            }
            with(binding){
                animationView.pauseAnimation()
                animationView.cancelAnimation()
                animationView.visibility = View.INVISIBLE
                scrollView.visibility = View.VISIBLE
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage, Toast.LENGTH_LONG).show()
        }
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
        val imageName = "${auth.currentUser!!.uid}.jpg"
        val imageReference = storage.reference.child("profilePhotos").child(imageName)
        val username = binding.nameEditText.text.toString()
        val country = binding.countrySpinner.selectedItem.toString()
        val gender = binding.genderSpinner.selectedItem.toString()
        val biography = binding.bioEditText.text.toString()
        imageReference.delete().addOnSuccessListener {
                val newImageReference = storage.reference.child("profilePhotos").child(imageName)
                if (selectedPicture != null) {
                    newImageReference.putFile(selectedPicture!!).addOnSuccessListener {
                        imageReference.downloadUrl.addOnSuccessListener { uri->
                            val ppUri = uri.toString()
                            val user = hashMapOf<String,Any>()
                            user["userUid"] = auth.currentUser!!.uid
                            user["profileUrl"] = ppUri
                            user["username"] = username
                            user["country"] = country
                            user["gender"] = gender
                            user["biography"] = biography
                            user["phone"] = phone as Any
                            user["speech"] = ""
                            firestore.collection("Users").document(auth.currentUser!!.uid).set(user).addOnSuccessListener {
                                Toast.makeText(requireContext(),"Profile update has been done", Toast.LENGTH_LONG).show()
                                parentFragmentManager.popBackStack()
                            }.addOnFailureListener {
                                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                            }

                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }

        }.addOnFailureListener {
                Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }

    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                selectedPicture!!
                            )
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.editProfilePhotoImageView.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                selectedPicture
                            )
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