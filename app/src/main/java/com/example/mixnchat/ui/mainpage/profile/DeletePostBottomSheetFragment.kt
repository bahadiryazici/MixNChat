package com.example.mixnchat.ui.mainpage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentDeletePostBottomSheetBinding
import com.example.mixnchat.utils.AndroidUtil
import com.example.mixnchat.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment



class DeletePostBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding : FragmentDeletePostBottomSheetBinding ?= null
    private val binding get() = _binding!!
    private val androidUtil = AndroidUtil()
    private lateinit var viewModel: DeletePostBottomSheetViewModel
    companion object {
        fun newInstance(postId: String): DeletePostBottomSheetFragment {
            val args = Bundle().apply {
                putString(Constants.ARG_POST_ID, postId)
            }
            val fragment = DeletePostBottomSheetFragment()
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeletePostBottomSheetBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[DeletePostBottomSheetViewModel::class.java]
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteCardView.setOnClickListener {
            deletePost()
        }
    }
    private fun deletePost(){
        val argPostId = arguments?.getString(Constants.ARG_POST_ID)
        viewModel.deletePost(argPostId!!,
            onError = {
            androidUtil.showToast(requireContext(),it)
        }, onSuccess = {
            androidUtil.showToast(requireContext(),this.getString(R.string.postDeletedMessage))
            dismiss()
        })
    }
}