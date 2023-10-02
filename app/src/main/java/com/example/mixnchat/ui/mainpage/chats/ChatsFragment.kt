package com.example.mixnchat.ui.mainpage.chats

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mixnchat.data.ChatRoomModel
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.FragmentChatsBinding
import com.example.mixnchat.utils.AndroidUtil
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
    private val androidUtil = AndroidUtil()
    private lateinit var viewModel: ChatsViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }
    private fun init() {
        viewModel = ViewModelProvider(this)[ChatsViewModel::class.java]
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
        showHeader()
    }

    private fun showHeader(){
        binding.searchView.setOnSearchClickListener {
            binding.textView33.visibility = View.INVISIBLE
        }
        binding.searchView.setOnCloseListener {
            binding.textView33.visibility = View.VISIBLE
            false
        }
    }

    private fun setUpChatsRecyclerAdapter() {
        val query = firebaseUtil.allChatRoomCollectionReference()
            .whereArrayContains("userIds",firebaseUtil.currentUserId())
            .orderBy("lastMessageTimeStamp", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(query,ChatRoomModel::class.java).build()
       updateRecyclerView(options)
    }

    private fun updateRecyclerView(options: FirestoreRecyclerOptions<ChatRoomModel>){
        adapter = ChatsAdapter(options, firebaseUtil,mcontext)
        binding.chatsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.chatsRecyclerView.adapter = adapter
        adapter!!.startListening()
    }

    private fun getFollowingUsers(){
        viewModel.getFollowingUsers(
            {
            followingList = it
            },
            onError = {androidUtil.showToast(requireContext(),it)})
        binding.followingRecyclerView.layoutManager = LinearLayoutManager(mcontext, LinearLayoutManager.HORIZONTAL,false)
        val followingAdapter = FollowingAdapter(followingList)
        binding.followingRecyclerView.adapter = followingAdapter
        followingAdapter.notifyDataSetChanged()
    }


}