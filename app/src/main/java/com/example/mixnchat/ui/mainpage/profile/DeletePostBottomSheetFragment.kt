package com.example.mixnchat.ui.mainpage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mixnchat.databinding.FragmentDeletePostBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class DeletePostBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding : FragmentDeletePostBottomSheetBinding ?= null
    private val binding get() = _binding!!
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth : FirebaseAuth


    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: String): DeletePostBottomSheetFragment {
            val args = Bundle().apply {
                putString(ARG_POST_ID, postId)
            }
            val fragment = DeletePostBottomSheetFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        _binding = FragmentDeletePostBottomSheetBinding.inflate(layoutInflater, container, false)
        init()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteCardView.setOnClickListener {
            deletePost()
        }
    }

    private fun init() {
        firestore = Firebase.firestore
        storage =Firebase.storage
        auth = Firebase.auth
    }

    private fun deletePost(){
        val argPostId = arguments?.getString(ARG_POST_ID)
        val imageName = "$argPostId.jpg"
        val imageReference = storage.reference.child("Posts").child(auth.currentUser!!.uid).child(imageName)
        imageReference.delete().addOnSuccessListener {
            if (argPostId != null) {
                firestore.collection(auth.currentUser!!.uid + "Posts").document(argPostId).delete().addOnSuccessListener {
                    Toast.makeText(requireContext(),"Post Deleted",Toast.LENGTH_LONG).show()
                    dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}