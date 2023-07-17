package com.example.mixnchat.onboarding.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentSecondScreenBinding


class SecondScreen : Fragment() {

    private var _binding : FragmentSecondScreenBinding?= null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentSecondScreenBinding.inflate(layoutInflater, container, false)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.button.setOnClickListener {
            viewPager?.currentItem = 2
        }

        return binding.root
    }

}