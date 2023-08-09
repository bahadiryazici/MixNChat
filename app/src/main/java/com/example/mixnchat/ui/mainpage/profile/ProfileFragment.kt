package com.example.mixnchat.ui.mainpage.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.Posts
import com.example.mixnchat.databinding.FragmentProfileBinding
import com.example.mixnchat.ui.Settings.SettingsActivity
import com.example.mixnchat.utils.AndroidUtil
import com.example.mixnchat.utils.FirebaseUtil
import java.util.UUID

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding ?= null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherForBackground: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var mcontext: Context
    private var selectedPicture : Uri? = null
    private var backgroundPhoto : Uri? = null
    private val androidUtil = AndroidUtil()
    private val firebaseUtil = FirebaseUtil()
    private lateinit var profilePostAdapter : ProfilePostAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        mcontext = requireContext()
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
        binding.settings.setOnClickListener {
            val intent = Intent(requireActivity(), SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            getBackgroundImage()
            getProfile()
            getPosts()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun addPhoto() {
     androidUtil.askPermission(mcontext,requireActivity(),requireView(),permissionLauncher,activityResultLauncher)
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
               androidUtil.showToast(mcontext,this.getString(R.string.permissionNeedMessage))
            }
        }
    }

    private fun uploadPhoto(){

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"
        val imageReference = firebaseUtil.getPostsFromStorage().child(firebaseUtil.currentUserId()).child(imageName)

        if (selectedPicture!=null){
            imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val postUri = uri.toString()
                    val uuidString = uuid.toString()
                    val userPost = hashMapOf<String,Any>()
                    userPost["Post"] = postUri
                    userPost["Uid"] = uuidString

                    firebaseUtil.getCurrentUserPosts().document(uuidString).set(userPost).addOnSuccessListener {
                        getPosts()
                    }.addOnFailureListener {
                        androidUtil.showToast(mcontext,it.localizedMessage!!)
                    }
                }.addOnFailureListener {
                    androidUtil.showToast(mcontext,it.localizedMessage!!)
                }
            }.addOnFailureListener{
                androidUtil.showToast(mcontext,it.localizedMessage!!)
            }
        }
    }
    private fun getPosts() {
        val postList = ArrayList<Posts>()
        firebaseUtil.getCurrentUserPosts().addSnapshotListener { value, error ->
            if (error != null){
                Toast.makeText(mcontext,error.localizedMessage,Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (value != null && !value.isEmpty){
                val documents = value.documents
                for (document in documents){
                    val postValue = document.getString("Post")
                    val postId = document.getString("Uid")
                    val post = Posts(postValue, postId)
                    postList.add(post)
                }
                binding.recyclerView.layoutManager = GridLayoutManager(mcontext,3,GridLayoutManager.VERTICAL,false)
                profilePostAdapter = ProfilePostAdapter(postList, object : OnPostItemClickListener {
                    override fun onPostItemClick(postId: String) {
                        val bottomSheetFragment = DeletePostBottomSheetFragment.newInstance(postId)
                        bottomSheetFragment.show(parentFragmentManager,bottomSheetFragment.tag)
                    }
                })
                binding.recyclerView.adapter = profilePostAdapter
                profilePostAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun getProfile(){
        androidUtil.startAnimation(binding.animationView,binding.scrollView)
       firebaseUtil.getAllUser().document(firebaseUtil.currentUserId()).get().addOnSuccessListener {
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
                Glide.with(this).asBitmap().load(profileUrl).into(binding.photoProfile)
            }else{
                androidUtil.showToast(mcontext,this.getString(R.string.noUserMessage))

            }
           androidUtil.stopAnimation(binding.animationView,binding.scrollView)
        }.addOnFailureListener {
            androidUtil.showToast(mcontext,it.localizedMessage!!)
        }

        firebaseUtil.getCurrentUserFollowing().get().addOnSuccessListener {
            val followingCount = it.size()
            binding.followingCountText.text = followingCount.toString()
        }.addOnFailureListener {
            androidUtil.showToast(mcontext,it.localizedMessage!!)
        }

        firebaseUtil.getCurrentUserFollowers().get().addOnSuccessListener {
            val followersCount = it.size()
            binding.followersCountText.text = followersCount.toString()
        }.addOnFailureListener {
            androidUtil.showToast(mcontext,it.localizedMessage!!)
        }
    }

    private fun setBackgroundImage(){
        androidUtil.askPermission(mcontext,requireActivity(),requireView(),permissionLauncher,activityResultLauncherForBackground)
    }

    private fun uploadBackgroundPhoto(){

        val imageName = "${firebaseUtil.currentUserId()}.jpg"
        val imageReference = firebaseUtil.getBackgroundPhotosFromStorage().child(firebaseUtil.currentUserId()).child(imageName)
        if (backgroundPhoto!=null){
            imageReference.putFile(backgroundPhoto!!).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val userBackgroundImage = uri.toString()
                    val userBackground = hashMapOf<String,Any>()
                    userBackground["Background"] = userBackgroundImage

                   firebaseUtil.getCurrentUserBackground().document(firebaseUtil.currentUserId()).set(userBackground).addOnSuccessListener {
                        getBackgroundImage()

                    }.addOnFailureListener {
                        androidUtil.showToast(mcontext,it.localizedMessage!!)
                    }
                }.addOnFailureListener {
                    androidUtil.showToast(mcontext,it.localizedMessage!!)
                }
            }.addOnFailureListener{
                androidUtil.showToast(mcontext,it.localizedMessage!!)
            }
        }
    }

    private fun getBackgroundImage() {
      firebaseUtil.getCurrentUserBackground().document(firebaseUtil.currentUserId()).get().addOnSuccessListener {
            if(it.exists()){
                val backgroundImageUrl = it.getString("Background")
                Glide.with(this).asBitmap().load(backgroundImageUrl).into(binding.imageView)
            }
        }.addOnFailureListener {
            androidUtil.showToast(mcontext,it.localizedMessage!!)
        }
    }
}