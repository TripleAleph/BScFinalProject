package com.example.pawsitivelife.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentDogProfileBinding
import com.example.pawsitivelife.ui.mydogs.DogInfoAdapter
import com.example.pawsitivelife.ui.mydogs.DogInfoItem
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

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

        val name = arguments?.getString("name") ?: "Unknown"
        val birthDate = arguments?.getString("dateOfBirth") ?: "-"
        val gender = arguments?.getString("gender") ?: "-"
        val imageUrl = arguments?.getString("imageUrl")
        val breed = arguments?.getString("breed") ?: "-"
        val color = arguments?.getString("color") ?: "-"
        val neutered = arguments?.getBoolean("neutered") ?: false
        val microchipped = arguments?.getBoolean("microchipped") ?: false

        binding.profileLBLName.text = name

        Glide.with(this)
            .load(imageUrl?.takeIf { it.isNotEmpty() })
            .placeholder(R.drawable.missing_img_dog)
            .centerCrop()
            .into(binding.profileIMGDog)

        val calculatedAge = calculateDogAge(birthDate)
        val formattedBirthDate = formatBirthDate(birthDate)

        binding.root.findViewById<TextView>(R.id.age_value)?.text = calculatedAge
        binding.root.findViewById<TextView>(R.id.gender_value)?.text = gender

        val infoList = listOf(
            DogInfoItem("Date of Birth", formattedBirthDate),
            DogInfoItem("Breed", breed),
            DogInfoItem("Color", color),
            DogInfoItem("Neutered", if (neutered) "Yes" else "No"),
            DogInfoItem("Microchipped", if (microchipped) "Yes" else "No")
        )

        binding.profileLSTInfo.adapter = DogInfoAdapter(infoList)
    }

    private fun formatBirthDate(dateStr: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            val date = LocalDate.parse(dateStr, inputFormatter)
            outputFormatter.format(date)
        } catch (e: Exception) {
            "-"
        }
    }

    private fun calculateDogAge(birthDateString: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(birthDateString, formatter)
            val today = LocalDate.now()
            val age = Period.between(birthDate, today)
            val years = age.years
            val months = age.months

            when {
                years > 0 && months > 0 -> "$years years and $months months"
                years > 0 -> "$years years"
                months > 0 -> "$months months"
                else -> "Less than a month"
            }
        } catch (e: Exception) {
            "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
