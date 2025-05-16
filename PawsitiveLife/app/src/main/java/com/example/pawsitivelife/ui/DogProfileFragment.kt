package com.example.pawsitivelife.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentDogProfileBinding

class DogProfileFragment : Fragment() {

    private var _binding: FragmentDogProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDogProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve dog data from arguments
        val name = arguments?.getString("name") ?: "Unknown"
        val age = arguments?.getString("dateOfBirth") ?: "-"
        val gender = arguments?.getString("gender") ?: "-"
        val imageResId = arguments?.getInt("imageResId") ?: R.drawable.img_chubbie

        // Set UI elements with received dog data
        binding.profileLBLName.text = name
        binding.profileIMGDog.setImageResource(imageResId)

        val ageTextView = binding.root.findViewById<TextView>(R.id.age_value)
        val genderTextView = binding.root.findViewById<TextView>(R.id.gender_value)

        ageTextView?.text = age
        genderTextView?.text = gender
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
