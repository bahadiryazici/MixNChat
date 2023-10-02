package com.example.mixnchat.ui.mainpage.shuffle

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.Posts
import com.example.mixnchat.databinding.FragmentReviewedProfilPageBinding
import com.example.mixnchat.ui.mainpage.chats.ChatActivity
import com.example.mixnchat.ui.mainpage.profile.ProfilePostAdapter
import com.example.mixnchat.ui.mainpage.profile.OnPostItemClickListener
import com.example.mixnchat.utils.AndroidUtil
import com.example.mixnchat.utils.FirebaseUtil


class ReviewedProfilePage : Fragment(){

    private var _binding : FragmentReviewedProfilPageBinding ?= null
    private val binding get() = _binding!!
    private var userUid : String ?= null
    private lateinit var profilePostAdapter : ProfilePostAdapter
    private var username : String ?= null
    private var profileUrl : String ?= null
    private val androidUtil = AndroidUtil()
    private val firebaseUtil = FirebaseUtil()
    private lateinit var viewModel: ReviewedProfileViewModel
    private val postList = ArrayList<Posts>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
       _binding = FragmentReviewedProfilPageBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        viewModel = ViewModelProvider(this)[ReviewedProfileViewModel::class.java]
        getArgs()
        setReviewedProfile()
        setBackgroundImage()
        setPost()
        reViewMyPage()
        blockedUser()
    }

    private fun getArgs(){
        arguments?.let {
            userUid = ReviewedProfilePageArgs.fromBundle(it).userUid
        }
    }
    private fun setPost() {
      viewModel.setPosts(onError = {},
          {posts ->
          postList.clear()
          postList.addAll(posts)
          binding.recyclerView.layoutManager = GridLayoutManager(requireContext(),3,GridLayoutManager.VERTICAL,false)
          profilePostAdapter = ProfilePostAdapter(postList, object : OnPostItemClickListener { override fun onPostItemClick(postId: String) {} })
          binding.recyclerView.adapter = profilePostAdapter
          profilePostAdapter.notifyDataSetChanged()
      }, userUid!!)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView5.setOnClickListener {
            showPopup(it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        binding.chat.setOnClickListener {
            val intent = Intent(requireContext(),ChatActivity::class.java)
            intent.putExtra("username",username)
            intent.putExtra("profileUrl",profileUrl)
            intent.putExtra("userId",userUid)
            startActivity(intent)
        }

    }

    private fun swipeRefresh() {
        setReviewedProfile()
        setBackgroundImage()
        setPost()
        reViewMyPage()
        blockedUser()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(requireContext(), v)
        val inflater : MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu,popup.menu)
        popUpMenuStatement(popup)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.follow ->{
                    val user = hashMapOf<String,Any>()
                    user["username"] = username.toString()
                    user["profileUrl"] = profileUrl.toString()
                    user["userUid"] = userUid.toString()
                    firebaseUtil.getCurrentUserFollowing().document(userUid!!).set(user)
                    firebaseUtil.getOtherUserFollowers(userUid!!).document(firebaseUtil.currentUserId()).set(hashMapOf("timestamp" to System.currentTimeMillis()))
                }
                R.id.block ->{
                    firebaseUtil.getCurrentUserBlocks().document(userUid!!).set(hashMapOf("timestamp" to System.currentTimeMillis()))
                    firebaseUtil.getCurrentUserFollowing().document(userUid!!).delete()
                    firebaseUtil.getOtherUserFollowers(userUid!!).document(firebaseUtil.currentUserId()).delete()
                }
                R.id.unFollow ->{
                    firebaseUtil.getCurrentUserFollowing().document(userUid!!).delete()
                    firebaseUtil.getOtherUserFollowers(userUid!!).document(firebaseUtil.currentUserId()).delete()
                }
                R.id.unBlock->{
                    firebaseUtil.getCurrentUserBlocks().document(userUid!!).delete()
                }
            }
            true
        }
        popup.show()
    }

    private fun popUpMenuStatement(popup : PopupMenu){
        firebaseUtil.getCurrentUserFollowing().document(userUid!!).get().addOnSuccessListener { documentSnapshot ->
            if(documentSnapshot.exists()){
                firebaseUtil.getCurrentUserBlocks().document(userUid!!).get().addOnSuccessListener {
                    if(it.exists()){
                        popup.menu.removeItem(R.id.block)
                        popup.menu.removeItem(R.id.follow)
                    }else{
                        popup.menu.removeItem(R.id.unBlock)
                        popup.menu.removeItem(R.id.follow)
                    }
                }
            }else{
                firebaseUtil.getCurrentUserBlocks().document(userUid!!).get().addOnSuccessListener {
                    if (it.exists()){
                        popup.menu.removeItem(R.id.block)
                        popup.menu.removeItem(R.id.unFollow)
                        popup.menu.removeItem(R.id.follow)
                    }else{
                        popup.menu.removeItem(R.id.unBlock)
                        popup.menu.removeItem(R.id.unFollow)
                    }
                }
            }
        }
    }

    private fun setReviewedProfile(){
        androidUtil.startAnimation(binding.animationView,binding.scrollView)
        if(userUid != null){
            viewModel.setReviewedProfile(userUid!!,{
                profileUrl = it[0]
                Glide.with(this).asBitmap().load(profileUrl).into(binding.photoProfile)
                username = it[1]
                if(it[4] == "")
                    binding.speechCountText.text ="0"
                else
                    binding.speechCountText.text = it[4]
                if (it[2] == "")
                    binding.textView10.text = "Unknown"
                else
                    binding.textView10.text = it[2]
                if (it[3] == "")
                    binding.textView25.text = "Unknown"
                else
                    binding.textView25.text = it[3]
            }, onError = {androidUtil.showToast(requireContext(),it)})
        }
        viewModel.getOtherUserFollowers(userUid!!, onError = {androidUtil.showToast(requireContext(),it) },
            {size ->
            binding.followersCountText.text = size.toString()
        })

        viewModel.getOtherUsersFollowing(userUid!!, onError = {androidUtil.showToast(requireContext(),it)},
            {size ->
                binding.followingCountText.text = size.toString()
            })
        androidUtil.stopAnimation(binding.animationView,binding.scrollView)
    }

    private fun reViewMyPage(){
        if(userUid == firebaseUtil.currentUserId()){
            binding.imageView5.visibility = View.INVISIBLE
            binding.chat.visibility = View.INVISIBLE
        }
    }

    private fun blockedUser(){
        viewModel.getOtherUserBlock(userUid!!, onError = {androidUtil.showToast(requireContext(), it)}, onSuccess = {
            if(it == firebaseUtil.currentUserId()){
                with(binding){

                    chat.visibility = View.INVISIBLE
                    photoProfile.setImageResource(R.drawable.blocked_icon)
                    imageView.setImageResource(R.drawable.blocked_icon)
                    recyclerView.adapter = ProfilePostAdapter(arrayListOf(),object :
                        OnPostItemClickListener { override fun onPostItemClick(postId: String) {} })
                    androidUtil.showToast(requireContext(),requireActivity().getString(R.string.blockMessage))
                }
            }
        })

    }

    private fun setBackgroundImage(){
        viewModel.getOtherUserBackground(userUid!!, onSuccess = {
            Glide.with(this).asBitmap().load(it).into(binding.imageView)
        }, onError = {androidUtil.showToast(requireContext(), it) })
    }
}