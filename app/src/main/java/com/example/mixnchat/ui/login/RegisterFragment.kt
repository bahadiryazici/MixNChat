package com.example.mixnchat.ui.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mixnchat.MainActivity
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.util.UUID


class RegisterFragment : Fragment() {

    private var _binding : FragmentRegisterBinding?= null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedPicture : Uri? = null
    private var selectedBitmap : Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        auth= Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
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
    }

    private fun signUp() {

        val email = binding.emailEditText2.text.toString()
        val password = binding.passwordEditText.text.toString()
        val username = binding.usernameEditText.text.toString()
        val country = binding.countrySpinner.selectedItem.toString()
        val gender = binding.genderSpinner.selectedItem.toString()


        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val imageReference = storage.reference.child("profilePhotos").child(imageName)


       if (email == "" || password == ""){

           binding.emailEditText2.error = "Please fill this area!"
           binding.passwordEditText.error = "Please fill this area!"

       }else if(!binding.pp.isSelected){
           Toast.makeText(requireContext(),"Select a picture", Toast.LENGTH_LONG).show()

       }else{
           auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
               if(selectedPicture != null){

                   imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                       imageReference.downloadUrl.addOnSuccessListener { uri ->
                           val profileUrl = uri.toString()
                           val user = hashMapOf<String,Any>()
                           user["profileUrl"] = profileUrl
                           user["username"] = username
                           user["country"] = country
                           user["gender"] = gender

                           firestore.collection("Users").add(user).addOnSuccessListener {
                               val intent = Intent(requireActivity(), MainActivity::class.java)
                               startActivity(intent)
                               requireActivity().finish()
                           }.addOnFailureListener {
                               Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
                           }
                       }
                   }
               }


           }.addOnFailureListener {
               Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
           }
       }
    }

    private fun ppClicked(){
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

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
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