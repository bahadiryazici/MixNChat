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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mixnchat.ui.mainpage.MainActivity
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.util.concurrent.TimeUnit


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
    private var phone : String ?= null

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

        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val username = binding.usernameEditText.text.toString()
        val country = binding.countrySpinner.selectedItem.toString()
        val biography = binding.biographyEditText.text.toString()
        val gender = binding.genderSpinner.selectedItem.toString()
        val phone = "+90${binding.phoneEditText.text}"




       if (email == "" || password == "" || biography == ""){

           binding.emailEditText.error = "Please fill this area!"
           binding.passwordEditText.error = "Please fill this area!"
           binding.biographyEditText.error = "Please fill this area!"

       }else if(phone.length != 13 && binding.phoneEditText.text.isNotEmpty()){
           binding.phoneEditText.error = "Please enter valid phone number"
       }/*else if(binding.pp.isSelected == false){
           Toast.makeText(requireContext(),"Select a picture", Toast.LENGTH_LONG).show()

       }*/else{
               auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
                   val imageName = "${auth.currentUser!!.uid}.jpg"
                   val imageReference = storage.reference.child("profilePhotos").child(imageName)
                   if(selectedPicture != null){
                       auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                           if(it.isSuccessful) {
                               imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                                   imageReference.downloadUrl.addOnSuccessListener { uri ->
                                       val profileUrl = uri.toString()
                                       val user = hashMapOf<String, Any>()
                                       user["userUid"] = auth.currentUser!!.uid
                                       user["profileUrl"] = profileUrl
                                       user["username"] = username
                                       user["country"] = country
                                       user["gender"] = gender
                                       user["biography"] = biography
                                       user["phone"] = phone
                                       user["speech"] = ""
                                       firestore.collection("Users").document(auth.currentUser!!.uid).set(user).addOnSuccessListener {
                                           if(binding.phoneEditText.text.isEmpty()){
                                               val intent = Intent(requireActivity(),MainActivity::class.java)
                                               startActivity(intent)
                                               requireActivity().finish()
                                           }else{
                                               phoneVerify()
                                           }

                                       }.addOnFailureListener {
                                           Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                                       }
                                   }
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


    private fun phoneVerify(){
        phone = "+90${binding.phoneEditText.text}"
        if(phone!!.isNotEmpty()){
            if (phone!!.length == 13){
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone!!)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }else{
                binding.phoneEditText.error = "Please Enter Correct Number"
            }
        }else{
            binding.phoneEditText.error = "Please Enter Number"
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

        }
        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
            // Show a message and update the UI
        }
        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            val action = RegisterFragmentDirections.actionRegisterFragmentToOTPFragment(verificationId,token,phone!!)
            findNavController().navigate(action)
        }
    }


}