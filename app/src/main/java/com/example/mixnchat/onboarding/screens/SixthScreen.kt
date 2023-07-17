package com.example.mixnchat.onboarding.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mixnchat.MainActivity
import com.example.mixnchat.databinding.FragmentSixthScreenBinding


class SixthScreen : Fragment() {
    private var _binding : FragmentSixthScreenBinding?= null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    // Inflate the layout for this fragment
    {
        // Inflate the layout for this fragment
        _binding =  FragmentSixthScreenBinding.inflate(layoutInflater, container, false)


        binding.button.setOnClickListener {
           val intent = Intent(this.context,MainActivity::class.java)
            onBoardingFinished()
            startActivity(intent)
        }

        return binding.root
    }

    private fun onBoardingFinished(){
        val sp = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }

}