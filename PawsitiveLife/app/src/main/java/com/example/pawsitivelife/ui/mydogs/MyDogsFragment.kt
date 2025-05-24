package com.example.pawsitivelife.ui.mydogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.adapter.ReminderAdapter
import com.example.pawsitivelife.databinding.FragmentMyDogsBinding
import com.example.pawsitivelife.ui.viewmodel.ReminderViewModel
import java.time.LocalDateTime
import android.content.Context
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Period
import android.widget.Toast


class MyDogsFragment : Fragment() {

    private val reminderViewModel: ReminderViewModel by activityViewModels()

    private var _binding: FragmentMyDogsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderAdapter = ReminderAdapter()
        binding.myDogsLSTReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }

        reminderViewModel.reminders.observe(viewLifecycleOwner) { allReminders ->
            val upcoming = allReminders.filter { it.date.isAfter(LocalDateTime.now()) }
            reminderAdapter.submitList(upcoming)
        }

        // Or: backend: 🔽 New code to load saved dog profile
        val prefs = requireContext().getSharedPreferences("DogPrefs", Context.MODE_PRIVATE)
        val dogJson = prefs.getString("dog_profile", null)

        if (dogJson != null) {
            val gson = Gson()
            val dog = gson.fromJson(dogJson, Dog::class.java)

            // Show dog info (adjust to your layout)
            binding.dogNameTextView.text = dog.name
            binding.dogBreedTextView.text = dog.breed
            binding.dogAgeTextView.text = "Age: ${calculateAgeFromDate(dog.dateOfBirth)}"
        } else {
            Toast.makeText(requireContext(), "No dog profile found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateAgeFromDate(date: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val birthDate = LocalDate.parse(date, formatter)
        val today = LocalDate.now()
        return Period.between(birthDate, today).years
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
