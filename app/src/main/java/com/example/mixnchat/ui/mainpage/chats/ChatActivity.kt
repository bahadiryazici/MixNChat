package com.example.mixnchat.ui.mainpage.chats

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.example.mixnchat.data.ChatMessageModel
import com.example.mixnchat.databinding.ActivityChatBinding
import com.example.mixnchat.ui.mainpage.MainActivity
import com.example.mixnchat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query


class ChatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatBinding
    private var username : String ?= null
    private var profileUrl : String ?= null
    private var userUid : String ?= null
    private var chatRoomId : String ?= null
    private var adapter : ChatRecyclerAdapter ?= null
    private val firebaseUtil = FirebaseUtil()
    private var listenerRegistration : ListenerRegistration ?= null
    private lateinit var viewModel: ChatViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init() {
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        binding.backButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        getArgs()
        setUser()
        chatRoomId = firebaseUtil.getChatRoomId(firebaseUtil.currentUserId(),userUid!!)
        getChatRoomReference(chatRoomId!!)
        setUpChatRecyclerView()
        seenMessage()
    }
    private fun setUpChatRecyclerView() {
        val query = firebaseUtil.getRoom(chatRoomId!!)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java).build()
        adapter = ChatRecyclerAdapter(options, firebaseUtil)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true)
        adapter!!.startListening()
        scrollDownWhenMessage(adapter!!)
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
        viewModel.sendMessageToUser(message,chatRoomId!!,userUid!!, onSuccess = {
            binding.chatInput.setText("") }
        )
    }
    private fun getChatRoomReference(chatRoomId : String){
        viewModel.getChatRoomReference(chatRoomId,userUid!!)
    }
    private fun seenMessage() {
        viewModel.seenMessage(chatRoomId!!) {
            listenerRegistration = it
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
            Glide.with(this@ChatActivity).asBitmap().load(profileUrl).into(pp)
            userName.text = username
        }
    }
    private fun getArgs(){
        val extras = intent.extras
        if(extras!=null){
            userUid = extras.getString("userId").toString()
            username = extras.getString("username").toString()
            profileUrl = extras.getString("profileUrl")
        }
    }
    override fun onStop() {
        super.onStop()
        listenerRegistration!!.remove()
    }
}