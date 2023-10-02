package com.example.mixnchat.ui.mainpage.shuffle

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mixnchat.data.Users
import com.example.mixnchat.databinding.FragmentShuffleBinding
import com.example.mixnchat.utils.AndroidUtil


class ShuffleFragment : Fragment() {

    private var _binding: FragmentShuffleBinding? = null
    private val binding get() = _binding!!
    private lateinit var shuffleAdapter: ShuffleAdapter
    private lateinit var mcontext: Context
    var filteredList = ArrayList<Users>()
    private val userArrayList = ArrayList<Users>()
    val resultList = ArrayList<Users>()
    private val androidUtil = AndroidUtil()
    private lateinit var viewModel: ShuffleViewModel
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
        viewModel = ViewModelProvider(this)[ShuffleViewModel::class.java]
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
        viewModel.getUserByRandom(onError = {
            androidUtil.showToast(requireContext(),it)
        }) {users ->
            userArrayList.clear()
            userArrayList.addAll(users)
            updateRecyclerView(userArrayList)
        }
    }
    private fun searchInFirebase(){

        viewModel.getUser(onError = {
            androidUtil.showToast(requireContext(),it)
        }){users ->
        resultList.clear()
        resultList.addAll(users)
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