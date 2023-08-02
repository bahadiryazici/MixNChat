package com.example.mixnchat.ui.mainpage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.FragmentShuffleBinding
import com.example.mixnchat.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Random


class ShuffleFragment : Fragment() {

    private var _binding : FragmentShuffleBinding ?= null
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var shuffleAdapter: ShuffleAdapter
    private lateinit var mcontext : Context
    val userArrayList = ArrayList<Users>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentShuffleBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        mcontext = requireContext()
        db = Firebase.firestore
        auth = Firebase.auth



        binding.button3.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }
        getUser()

        binding.swipeRefreshLayout.setOnRefreshListener {
            getUser()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(mcontext)
        shuffleAdapter = ShuffleAdapter(userArrayList)
        binding.recyclerView.adapter = shuffleAdapter
    }


    private fun getUser(){


        db.collection("Users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(mcontext, error.localizedMessage, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {


                val documents = snapshot.documents

                val allUsers = ArrayList<Users>()
                for (document in documents) {
                    val userName = document.getString("username")
                    val biography = document.getString("biography")
                    val profileUrl = document.getString("profileUrl")
                    val userUid = document.getString("userUid")
                    if (userName != null && biography != null && profileUrl != null) {
                        val user = Users(userName, biography, profileUrl, userUid)
                        allUsers.add(user)
                    }
                }

                val random = Random()
                while (userArrayList.size < 5 && userArrayList.size < allUsers.size) {
                    val randomIndex = random.nextInt(allUsers.size)
                    val randomUser = allUsers[randomIndex]
                    if (!userArrayList.contains(randomUser)) {
                        userArrayList.add(randomUser)
                    }
                }
                shuffleAdapter.notifyDataSetChanged()
            }
        }

    }
}