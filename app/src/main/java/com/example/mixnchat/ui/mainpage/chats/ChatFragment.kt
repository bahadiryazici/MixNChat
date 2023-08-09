package com.example.mixnchat.ui.mainpage.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.example.mixnchat.R
import com.example.mixnchat.data.ChatMessageModel
import com.example.mixnchat.data.ChatRoomModel
import com.example.mixnchat.databinding.FragmentChatBinding
import com.example.mixnchat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query


class ChatFragment : Fragment() {


    private var _binding : FragmentChatBinding ?= null
    private val binding get() = _binding!!
    private var username : String ?= null
    private var profileUrl : String ?= null
    private var userUid : String ?= null
    private var chatRoomId : String ?= null
    private var chatRoomModel : ChatRoomModel ?=null
    private var adapter : ChatRecyclerAdapter ?= null
    private val firebaseUtil = FirebaseUtil()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.GONE
        getArgs()
        setUser()
        chatRoomId = firebaseUtil.getChatRoomId(firebaseUtil.currentUserId(),userUid!!)
        getChatRoomReference(chatRoomId!!)
        setUpChatRecyclerView()
    }

    private fun setUpChatRecyclerView() {
        val query = firebaseUtil.getRoom(chatRoomId!!).collection("chats").orderBy("timestamp", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java).build()
        adapter = ChatRecyclerAdapter(options)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,true)
        adapter!!.startListening()
        scrollDownWhenMessage(adapter!!)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val message = binding.chatInput.text.toString()
        if (message.isEmpty()){
            return
        }else{
            sendMessageToUser(message)
        }
    }

    private fun sendMessageToUser(message : String) {

        chatRoomModel?.lastMessageTimeStamp = Timestamp.now()
        chatRoomModel?.lastMessageSenderId = firebaseUtil.currentUserId()
        chatRoomModel?.lastMessage = message
        firebaseUtil.allChatRoomCollectionReference().document(chatRoomId!!).set(chatRoomModel!!)

        val chatMessageModel = ChatMessageModel(message,firebaseUtil.currentUserId(), Timestamp.now())

        firebaseUtil.getRoom(chatRoomId!!).collection("chats").add(chatMessageModel).addOnCompleteListener {
            if (it.isSuccessful){
                binding.chatInput.setText("")
            }
        }
    }


    private fun getChatRoomReference(chatRoomId : String){
        firebaseUtil.allChatRoomCollectionReference().document(chatRoomId).get().addOnCompleteListener {task ->
            if (task.isSuccessful){
                chatRoomModel = task.result.toObject(ChatRoomModel::class.java)
                if (chatRoomModel == null){
                    //first  time chat
                    chatRoomModel = ChatRoomModel(
                        chatRoomId,
                        listOf(firebaseUtil.currentUserId(), userUid) as List<String>,
                        Timestamp.now(),
                        ""
                    )
                    firebaseUtil.allChatRoomCollectionReference().document(chatRoomId).set(chatRoomModel!!)
                }
            }

        }
    }

    private fun scrollDownWhenMessage(adapter : ChatRecyclerAdapter){
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.chatRecyclerView.smoothScrollToPosition(0)
            }
        })
    }

    private fun setUser(){
        with(binding){
            Glide.with(requireContext()).asBitmap().load(profileUrl).into(pp)
            userName.text = username
        }
    }

    private fun getArgs(){
        arguments?.let {
            userUid = ChatFragmentArgs.fromBundle(it).userid
            username = ChatFragmentArgs.fromBundle(it).username
            profileUrl = ChatFragmentArgs.fromBundle(it).profileUrl
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

}