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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.Posts
import com.example.mixnchat.databinding.FragmentProfileBinding
import com.example.mixnchat.ui.settings.SettingsActivity
import com.example.mixnchat.utils.AndroidUtil

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
    private lateinit var profilePostAdapter : ProfilePostAdapter
    private lateinit var viewModel: ProfileViewModel
    private val postList = ArrayList<Posts>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        mcontext = requireContext()
        registerLauncher()
        getBackgroundImage()
        getProfile()
        getPosts()
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
     viewModel.uploadPhoto(
         onError = { androidUtil.showToast(mcontext,it)},
         onSuccess = { getPosts() },
         selectedPicture)
    }
    private fun getPosts() {
       viewModel.getPosts(onError = { androidUtil.showToast(mcontext,it) })
       {posts ->
           postList.clear()
           postList.addAll(posts)
           binding.recyclerView.layoutManager = GridLayoutManager(mcontext,3,GridLayoutManager.VERTICAL,false)
           profilePostAdapter = ProfilePostAdapter(postList, object :  OnPostItemClickListener {
               override fun onPostItemClick(postId: String) {
                   val bottomSheetFragment = DeletePostBottomSheetFragment.newInstance(postId)
                   bottomSheetFragment.show(parentFragmentManager,bottomSheetFragment.tag)
               }
           })
           profilePostAdapter.notifyDataSetChanged()
           binding.recyclerView.adapter = profilePostAdapter
       }
    }

    private fun getProfile(){
        androidUtil.startAnimation(binding.animationView,binding.scrollView)
        viewModel.errorMessage = getString(R.string.noUserMessage)
        viewModel.getProfile(onError = {
            androidUtil.showToast(mcontext,it)
        }, onSuccess = {
            val profileUrl = it[0]
            Glide.with(this).asBitmap().load(profileUrl).into(binding.photoProfile)
            if (it[4] == ""){
                binding.speechCountText.text = "0"
            }else{
                binding.speechCountText.text = it[4]
            }
            if (it[2] == ""){
                binding.textView11.text = "Unknown"
            }else{
                binding.textView11.text = it[2]
            }
            if (it[3] == ""){
                binding.textView25.text = "Unknown"
            }else{
                binding.textView25.text = it[3]
            }
            binding.textView10.text = it[1]
        })
        viewModel.getCurrentUserFollowing(onError = {
            androidUtil.showToast(mcontext,it)
        }, onSuccess = {
            binding.followingCountText.text = it.toString()
        })
        viewModel.getCurrentUserFollowers(onError = {
            androidUtil.showToast(mcontext,it)
        }, onSuccess = {
            binding.followersCountText.text = it.toString()
        })

        androidUtil.stopAnimation(binding.animationView,binding.scrollView)
    }
    private fun setBackgroundImage(){
        androidUtil.askPermission(mcontext,requireActivity(),requireView(),permissionLauncher,activityResultLauncherForBackground)
    }
    private fun uploadBackgroundPhoto(){
        viewModel.backgroundPhoto(
            onError = {
            androidUtil.showToast(mcontext,it)
        }, onSuccess = {
            getBackgroundImage()
        },backgroundPhoto)
    }
    private fun getBackgroundImage() {
        viewModel.getBackgroundImage(
            onError = {
            androidUtil.showToast(mcontext,it)
        }, onSuccess = {
            Glide.with(this).asBitmap().load(it).into(binding.imageView)
        })
    }
}