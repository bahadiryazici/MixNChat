package com.example.mixnchat.ui.mainpage.shuffle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.Posts
import com.example.mixnchat.databinding.FragmentReviewedProfilPageBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment

       _binding = FragmentReviewedProfilPageBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
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
        val postList = ArrayList<Posts>()
        firebaseUtil.getOtherUserPosts(userUid!!).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            if (value != null && !value.isEmpty) {
                val documents = value.documents

                for (document in documents) {
                    val postValue = document.getString("Post")
                    val postId = document.getString("Uid")
                    val post = Posts(postValue,postId)
                    postList.add(post)
                }
                binding.recyclerView.layoutManager = GridLayoutManager(requireContext(),3,GridLayoutManager.VERTICAL,false)
                profilePostAdapter = ProfilePostAdapter(postList, object :
                    OnPostItemClickListener { override fun onPostItemClick(postId: String) {} })
                binding.recyclerView.adapter = profilePostAdapter
                profilePostAdapter.notifyDataSetChanged()
            }
        }
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
            findNavController().navigate(ReviewedProfilePageDirections.actionReviewedProfilPageToChatFragment(username!!,profileUrl!!,userUid!!))
        }
    }

    private fun swipeRefresh() {
        androidUtil.startAnimation(binding.animationView,binding.scrollView)
        setReviewedProfile()
        setBackgroundImage()
        setPost()
        reViewMyPage()
        blockedUser()
        binding.swipeRefreshLayout.isRefreshing = false
        androidUtil.stopAnimation(binding.animationView,binding.scrollView)
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
        userUid?.let { it ->
            firebaseUtil.getAllUser().document(it).get().addOnSuccessListener {
                if(it.exists()){
                    profileUrl = it.getString("profileUrl")
                    username = it.getString("username")
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
                    androidUtil.showToast(requireContext(),"No users")
                }
              androidUtil.stopAnimation(binding.animationView,binding.scrollView)
            }.addOnFailureListener { error ->
                androidUtil.showToast(requireContext(),error.localizedMessage!!)
            }
        }
        firebaseUtil.getOtherUserFollowers(userUid!!).get().addOnSuccessListener {
            val followers = it.size()
            binding.followersCountText.text = followers.toString()
        }.addOnFailureListener {
            androidUtil.showToast(requireContext(),it.localizedMessage!!)
        }
        firebaseUtil.getOtherUserFollowing(userUid!!).get().addOnSuccessListener {
            val following = it.size()
            binding.followingCountText.text = following.toString()
        }.addOnFailureListener {
            androidUtil.showToast(requireContext(),it.localizedMessage!!)
        }
    }

    private fun reViewMyPage(){
        if(userUid == firebaseUtil.currentUserId()){
            binding.imageView5.visibility = View.INVISIBLE
            binding.chat.visibility = View.INVISIBLE
        }
    }

    private fun blockedUser(){
        firebaseUtil.getOtherUserBlocks(userUid!!).document(firebaseUtil.currentUserId()).get().addOnSuccessListener {
            if(it.exists()){
                if (it.id == firebaseUtil.currentUserId()){
                    with(binding){

                        chat.visibility = View.INVISIBLE
                        photoProfile.setImageResource(R.drawable.blocked_icon)
                        imageView.setImageResource(R.drawable.blocked_icon)
                        recyclerView.adapter = ProfilePostAdapter(arrayListOf(),object :
                            OnPostItemClickListener { override fun onPostItemClick(postId: String) {} })
                        androidUtil.showToast(requireContext(), "You have been blocked!")
                    }
                }
            }

        }.addOnFailureListener {
            androidUtil.showToast(requireContext(),it.localizedMessage!!)
        }
    }

    private fun setBackgroundImage(){
        firebaseUtil.getOtherUserBackground(userUid!!).document(userUid!!).get().addOnSuccessListener {
            if(it.exists()){
                val profileBackgroundImageUrl = it.getString("Background")
                Glide.with(this).asBitmap().load(profileBackgroundImageUrl).into(binding.imageView)
            }
        }.addOnFailureListener {
            androidUtil.showToast(requireContext(),it.localizedMessage!!)
        }
    }

}