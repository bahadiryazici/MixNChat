package com.example.mixnchat.ui.mainpage.shuffle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.Posts
import com.example.mixnchat.databinding.FragmentReviewedProfilPageBinding
import com.example.mixnchat.ui.mainpage.ReviewedProfilePageArgs
import com.example.mixnchat.ui.mainpage.profile.ProfilePostAdapter
import com.example.mixnchat.ui.mainpage.profile.OnPostItemClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class ReviewedProfilePage : Fragment(){

    private var _binding : FragmentReviewedProfilPageBinding ?= null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private var userUid : String ?= null
    private lateinit var profilePostAdapter : ProfilePostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

       _binding = FragmentReviewedProfilPageBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {

        firestore = Firebase.firestore
        auth = Firebase.auth
        arguments?.let {
            userUid = ReviewedProfilePageArgs.fromBundle(it).userUid
        }
        setReviewedProfile()
        setBackgroundImage()
        setPost()
        reViewMyPage()
        blockedUser()
    }

    private fun setPost() {
        val postList = ArrayList<Posts>()
        firestore.collection(userUid!! + "Posts").addSnapshotListener { value, error ->
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
            setReviewedProfile()
            setBackgroundImage()
            setPost()
            reViewMyPage()
            blockedUser()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(requireContext(), v)
        val inflater : MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu,popup.menu)
        popUpMenuStatement(popup)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.follow ->{
                    firestore.collection(auth.currentUser!!.uid + "Following" ).document(userUid!!).set(hashMapOf("timestamp" to System.currentTimeMillis()))
                    firestore.collection(userUid + "Followers").document(auth.currentUser!!.uid).set(hashMapOf("timestamp" to System.currentTimeMillis()))
                }
                R.id.block ->{
                    firestore.collection(auth.currentUser!!.uid + "Blocks" ).document(userUid!!).set(hashMapOf("timestamp" to System.currentTimeMillis()))
                    firestore.collection(auth.currentUser!!.uid + "Following").document(userUid!!).delete()
                    firestore.collection(userUid + "Followers").document(auth.currentUser!!.uid).delete()
                }
                R.id.unFollow ->{
                    firestore.collection(auth.currentUser!!.uid + "Following").document(userUid!!).delete()
                    firestore.collection(userUid + "Followers").document(auth.currentUser!!.uid).delete()
                }
                R.id.unBlock->{
                    firestore.collection(auth.currentUser!!.uid + "Blocks").document(userUid!!).delete()
                }
            }
            true
        }
        popup.show()
    }

    private fun popUpMenuStatement(popup : PopupMenu){
        firestore.collection(auth.currentUser!!.uid + "Following").document(userUid!!).get().addOnSuccessListener {
            if(it.exists()){
                firestore.collection(auth.currentUser!!.uid + "Blocks").document(userUid!!).get().addOnSuccessListener {
                    if(it.exists()){
                        popup.menu.removeItem(R.id.block)
                        popup.menu.removeItem(R.id.follow)
                    }else{
                        popup.menu.removeItem(R.id.unBlock)
                        popup.menu.removeItem(R.id.follow)
                    }
                }
            }else{
                firestore.collection(auth.currentUser!!.uid + "Blocks").document(userUid!!).get().addOnSuccessListener {
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
        with(binding){
            animationView.playAnimation()
           scrollView.visibility = View.INVISIBLE
        }
        userUid?.let {
            firestore.collection("Users").document(it).get().addOnSuccessListener {
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
                with(binding){
                    animationView.pauseAnimation()
                    animationView.cancelAnimation()
                    animationView.visibility = View.INVISIBLE
                    scrollView.visibility = View.VISIBLE
                }
            }.addOnFailureListener { error ->
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
        firestore.collection(userUid + "Followers").get().addOnSuccessListener {
            val followers = it.size()
            binding.followersCountText.text = followers.toString()
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }
        firestore.collection(userUid + "Following").get().addOnSuccessListener {
            val following = it.size()
            binding.followingCountText.text = following.toString()
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }

    private fun reViewMyPage(){
        if(userUid == auth.currentUser!!.uid){
            binding.imageView5.visibility = View.INVISIBLE
            binding.imageView6.visibility = View.INVISIBLE
        }
    }

    private fun blockedUser(){
        firestore.collection(userUid + "Blocks").document(auth.currentUser!!.uid).get().addOnSuccessListener {
            if(it.exists()){
                if (it.id == auth.currentUser!!.uid){
                    with(binding){

                        imageView6.visibility = View.INVISIBLE
                        photoProfile.setImageResource(R.drawable.blocked_icon)
                        imageView.setImageResource(R.drawable.blocked_icon)
                        recyclerView.adapter = ProfilePostAdapter(arrayListOf(),object :
                            OnPostItemClickListener { override fun onPostItemClick(postId: String) {} })
                        Toast.makeText(requireContext(),"You have been blocked!", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }

    private fun setBackgroundImage(){
        firestore.collection(userUid!! + "Background").document(userUid!!).get().addOnSuccessListener {
            if(it.exists()){
                val profileBackgroundImageUrl = it.getString("Background")
                Glide.with(this).asBitmap().load(profileBackgroundImageUrl).into(binding.imageView)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }
}