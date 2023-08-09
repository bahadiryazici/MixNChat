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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.mixnchat.R
import com.example.mixnchat.ui.mainpage.MainActivity
import com.example.mixnchat.databinding.FragmentRegisterBinding
import com.example.mixnchat.utils.AndroidUtil
import com.example.mixnchat.utils.FirebaseUtil
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
    private val firebaseUtil = FirebaseUtil()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        auth= Firebase.auth
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


       if (email == "" || password == "" || biography == ""){

           binding.emailEditText.error = this.getString(R.string.fillFieldMessage)
           binding.passwordEditText.error = this.getString(R.string.fillFieldMessage)
           binding.biographyEditText.error = this.getString(R.string.fillFieldMessage)


       }else if(selectedPicture == null){
           androidUtil.showToast(requireContext(),this.getString(R.string.selectPictureMessage))

       }else{
               auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
                   val imageName = "${firebaseUtil.currentUserId()}.jpg"
                   val imageReference = firebaseUtil.getProfilePhotoFromStorage().child(imageName)
                   if(selectedPicture != null){
                       auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                           if(task.isSuccessful) {
                               imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                                   imageReference.downloadUrl.addOnSuccessListener { uri ->
                                       val profileUrl = uri.toString()
                                       val user = hashMapOf<String, Any>()
                                       user["userUid"] = firebaseUtil.currentUserId()
                                       user["profileUrl"] = profileUrl
                                       user["username"] = username
                                       user["country"] = country
                                       user["gender"] = gender
                                       user["biography"] = biography
                                       user["speech"] = ""
                                      firebaseUtil.getAllUser().document(firebaseUtil.currentUserId()).set(user).addOnSuccessListener {
                                          val intent = Intent(requireActivity(),MainActivity::class.java)
                                          startActivity(intent)
                                          requireActivity().finish()
                                       }.addOnFailureListener {
                                           androidUtil.showToast(requireContext(),it.localizedMessage!!)
                                       }
                                   }
                               }
                           }
                       }
                   }
               }.addOnFailureListener {
                   androidUtil.showToast(requireContext(),it.localizedMessage!!)
               }
       }
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