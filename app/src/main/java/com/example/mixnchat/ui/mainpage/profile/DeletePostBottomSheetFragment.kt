package com.example.mixnchat.ui.mainpage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentDeletePostBottomSheetBinding
import com.example.mixnchat.utils.AndroidUtil
import com.example.mixnchat.utils.Constants
import com.example.mixnchat.utils.FirebaseUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment



class DeletePostBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding : FragmentDeletePostBottomSheetBinding ?= null
    private val binding get() = _binding!!
    private val androidUtil = AndroidUtil()
    private val firebaseUtil = FirebaseUtil()


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
        val imageName = "$argPostId.jpg"
        val imageReference =firebaseUtil.getPostsFromStorage().child(firebaseUtil.currentUserId()).child(imageName)
        imageReference.delete().addOnSuccessListener {
            if (argPostId != null) {
                firebaseUtil.getCurrentUserPosts().document(argPostId).delete().addOnSuccessListener {
                    androidUtil.showToast(requireContext(),this.getString(R.string.postDeletedMessage))
                    dismiss()
                }.addOnFailureListener {
                    androidUtil.showToast(requireContext(),it.localizedMessage!!)
                }
            }
        }.addOnFailureListener {
            androidUtil.showToast(requireContext(),it.localizedMessage!!)
        }
    }
}