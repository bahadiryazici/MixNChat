package com.example.mixnchat.ui.mainpage

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.Posts
import com.example.mixnchat.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.UUID

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherForBackground: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedPicture : Uri? = null
    private var backgroundPhoto : Uri? = null
    private lateinit var profilePostAdapter : ProfilePostAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
        registerLauncher()
        getBackgroundImage()
        getPosts()
        getProfile()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addPhoto.setOnClickListener {
            addPhoto()
        }
        binding.imageView.setOnClickListener {
            setBackgroundImage()
        }
    }

    private fun addPhoto() {

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
            if (result.resultCode == Activity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                    uploadPhoto()

                }
            }
        }

        activityResultLauncherForBackground = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    backgroundPhoto = intentFromResult.data
                    uploadBackgroundPhoto()

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

    private fun uploadPhoto(){

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"
        val imageReference = storage.reference.child("Posts").child(auth.currentUser!!.uid).child(imageName)

        if (selectedPicture!=null){
            imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val postUri = uri.toString()
                    val userPost = hashMapOf<String,Any>()
                    userPost["Post"] = postUri

                    firestore.collection(auth.currentUser!!.uid).add(userPost).addOnSuccessListener {
                        getPosts()

                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener{
                Toast.makeText(requireContext(), it.localizedMessage,Toast.LENGTH_LONG ).show()
            }
        }
    }
    private fun getPosts() {
        val postList = ArrayList<Posts>()
        firestore.collection(auth.currentUser!!.uid).addSnapshotListener { value, error ->
            if (error != null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (value != null && !value.isEmpty){
                val documents = value.documents

                for (document in documents){
                    val postValue = document.getString("Post")
                    val post = Posts(postValue)
                    postList.add(post)
                }
                binding.recyclerView.layoutManager = GridLayoutManager(requireContext(),3,GridLayoutManager.VERTICAL,false)
                profilePostAdapter = ProfilePostAdapter(postList)
                binding.recyclerView.adapter = profilePostAdapter
                profilePostAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getProfile(){
        firestore.collection("Users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
            if(it.exists()){
                val profileUrl = it.getString("profileUrl")
                val username = it.getString("username")
                val country = it.getString("country")
                val biography = it.getString("biography")
                val speech = it.getString("speech")
                if (speech.equals("")){
                    binding.speechCountText.text = "0"
                }else{
                    binding.speechCountText.text = speech
                }
                if (country.equals("")){
                    binding.textView11.text = "Unknown"
                }else{
                    binding.textView11.text = country
                }
                if (biography.equals("")){
                    binding.textView25.text = "Unknown"
                }else{
                    binding.textView25.text = biography
                }
                binding.textView10.text = username
                Picasso.get().load(profileUrl).into(binding.photoProfile)
            }else{
                Toast.makeText(requireContext(),"No users", Toast.LENGTH_LONG).show()

            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }

        firestore.collection(auth.currentUser!!.uid + "Following").get().addOnSuccessListener {
            val followingCount = it.size()
            binding.followingCountText.text = followingCount.toString()
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }

        firestore.collection(auth.currentUser!!.uid + "Followers").get().addOnSuccessListener {
            val followersCount = it.size()
            binding.followersCountText.text = followersCount.toString()
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }
    private fun setBackgroundImage(){
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
            activityResultLauncherForBackground.launch(intentToGallery)

        }
    }

    private fun uploadBackgroundPhoto(){
        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val imageReference = storage.reference.child("BackgroundPhotos").child(auth.currentUser!!.uid).child(imageName)
        if (backgroundPhoto!=null){
            imageReference.putFile(backgroundPhoto!!).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val userBackgroundImage = uri.toString()
                    val userBackground = hashMapOf<String,Any>()
                    userBackground["Background"] = userBackgroundImage

                    firestore.collection(auth.currentUser!!.uid + "Background").document(auth.currentUser!!.uid).set(userBackground).addOnSuccessListener {
                        getBackgroundImage()

                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener{
                Toast.makeText(requireContext(), it.localizedMessage,Toast.LENGTH_LONG ).show()
            }
        }
    }

    private fun getBackgroundImage() {
        firestore.collection(auth.currentUser!!.uid + "Background").document(auth.currentUser!!.uid).get().addOnSuccessListener {
            if(it.exists()){
                val backgroundImageUrl = it.getString("Background")
                Glide.with(this).asBitmap().load(backgroundImageUrl).into(binding.imageView)
            }
        }.addOnFailureListener { error ->
            Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }
}