package com.example.mixnchat.ui.mainpage.chats

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mixnchat.data.ChatRoomModel
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.FragmentChatsBinding
import com.example.mixnchat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query



class ChatsFragment : Fragment() {


    private var _binding : FragmentChatsBinding?= null
    private val binding get() = _binding!!
    private var followingList = ArrayList<Users>()
    private lateinit var mcontext : Context
    private var adapter : ChatsAdapter ?= null
    private val firebaseUtil = FirebaseUtil()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }
    private fun init() {
        mcontext = requireContext()
        getFollowingUsers()
        setUpChatsRecyclerAdapter()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            getFollowingUsers()
            setUpChatsRecyclerAdapter()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setUpChatsRecyclerAdapter() {
        val query = firebaseUtil.allChatRoomCollectionReference()
            .whereArrayContains("userIds",firebaseUtil.currentUserId())
            .orderBy("lastMessageTimeStamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>().setQuery(query,ChatRoomModel::class.java).build()
        adapter = ChatsAdapter(options, firebaseUtil,requireContext())
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.chatsRecyclerView.adapter = adapter
        adapter!!.startListening()

    }


    private fun getFollowingUsers(){
        followingList.clear()
        firebaseUtil.getCurrentUserFollowing().addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            if(value != null && !value.isEmpty){
                val documents = value.documents
                for(document in documents){
                    val username = document.getString("username")
                    val pp = document.getString("profileUrl")
                    val userUid = document.getString("userUid")
                    if(username != null && pp != null && userUid != null){
                        val user = Users(username,null,pp,userUid)
                        followingList.add(user)
                    }
                }
            }

            binding.followingRecyclerView.layoutManager = LinearLayoutManager(mcontext, LinearLayoutManager.HORIZONTAL,false)
            val followingAdapter = FollowingAdapter(followingList,)
            binding.followingRecyclerView.adapter = followingAdapter
            followingAdapter.notifyDataSetChanged()


        }

    }


}