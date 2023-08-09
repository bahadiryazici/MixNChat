package com.example.mixnchat.ui.mainpage.shuffle

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.FragmentShuffleBinding
import com.example.mixnchat.utils.AndroidUtil
import com.example.mixnchat.utils.FirebaseUtil
import java.util.Random


class ShuffleFragment : Fragment() {

    private var _binding: FragmentShuffleBinding? = null
    private val binding get() = _binding!!
    private lateinit var shuffleAdapter: ShuffleAdapter
    private lateinit var mcontext: Context
    private val userArrayList = ArrayList<Users>()
    var filteredList = ArrayList<Users>()
    val resultList = ArrayList<Users>()
    private val androidUtil = AndroidUtil()
    private val firebaseUtil = FirebaseUtil()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentShuffleBinding.inflate(layoutInflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        mcontext = requireContext()
        getUser()
        binding.swipeRefreshLayout.setOnRefreshListener {
            swiperRefresh()
        }
        searchInFirebase()
        searchViewListener()
    }

    private fun swiperRefresh() {
        getUser()
        binding.searchView.setQuery("",false)
        binding.searchView.isIconified = true
        binding.swipeRefreshLayout.isRefreshing = false
    }
    private fun getUser() {
        firebaseUtil.getAllUser().addSnapshotListener { snapshot, error ->
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
                updateRecyclerView(userArrayList)
            }
        }
    }
    private fun searchInFirebase(){
        firebaseUtil.getAllUser().get().addOnSuccessListener {
            resultList.clear()
            for (document in it.documents) {
                val userName = document.getString("username")
                val biography = document.getString("biography")
                val profileUrl = document.getString("profileUrl")
                val userUid = document.getString("userUid")
                if (userName != null && biography != null && profileUrl != null) {
                    val user = Users(userName, biography, profileUrl, userUid)
                    resultList.add(user)
                }
            }
        }.addOnFailureListener {
            androidUtil.showToast(requireContext(),it.localizedMessage!!)
        }
    }
    private fun searchViewListener(){
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {
                if(p0!!.isEmpty()){
                    getUser()
                }else{
                    filteredList = resultList.filter {
                        it.username!!.lowercase().contains(p0.lowercase())
                    } as ArrayList<Users>
                    updateRecyclerView(filteredList )
                }
              return true
            }
        })
    }
    private fun updateRecyclerView(filteredList: ArrayList<Users>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(mcontext)
        shuffleAdapter = ShuffleAdapter(filteredList)
        binding.recyclerView.adapter = shuffleAdapter
        shuffleAdapter.notifyDataSetChanged()
    }
}