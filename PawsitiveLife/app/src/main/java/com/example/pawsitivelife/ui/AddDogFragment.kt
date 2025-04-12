package com.example.pawsitivelife.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.databinding.FragmentAddDogBinding
import com.example.pawsitivelife.R
class AddDogFragment : Fragment() {

    private var _binding: FragmentAddDogBinding? = null
    private val binding get() = _binding!!

    private var selectedGender: String? = null // "Male" or "Female"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // כפתור חזרה
        binding.addDogBTNBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // כפתורי מגדר
        binding.maleButton.setOnClickListener {
            selectGender("Male")
        }

        binding.femaleButton.setOnClickListener {
            selectGender("Female")
        }
    }

    private fun selectGender(gender: String) {
        selectedGender = gender

        val selectedColor = requireContext().getColor(R.color.french_rose_600)
        val defaultColor = requireContext().getColor(android.R.color.transparent)

        binding.maleButton.setBackgroundColor(if (gender == "Male") selectedColor else defaultColor)
        binding.femaleButton.setBackgroundColor(if (gender == "Female") selectedColor else defaultColor)

        binding.maleButton.setTextColor(
            if (gender == "Male") requireContext().getColor(R.color.white) else requireContext().getColor(R.color.black)
        )
        binding.femaleButton.setTextColor(
            if (gender == "Female") requireContext().getColor(R.color.white) else requireContext().getColor(R.color.black)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
