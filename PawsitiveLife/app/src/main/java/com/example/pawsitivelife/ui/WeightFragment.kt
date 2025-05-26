package com.example.pawsitivelife.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.databinding.FragmentWeightBinding
import com.example.pawsitivelife.model.WeightEntry
import com.example.pawsitivelife.ui.mydogs.WeightAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WeightFragment : Fragment() {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!

    private val calendar = Calendar.getInstance()
    private lateinit var adapter: WeightAdapter
    private val entries = mutableListOf<WeightEntry>()
    private lateinit var dogId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get dogId from arguments
        dogId = arguments?.getString("dogId") ?: run {
            Toast.makeText(requireContext(), "Missing dog ID", Toast.LENGTH_SHORT).show()
            Log.e("WeightDebug", "dogId is null or missing in arguments")
            return
        }

        // Initialize RecyclerView
        adapter = WeightAdapter(entries)
        binding.weightList.layoutManager = LinearLayoutManager(requireContext())
        binding.weightList.adapter = adapter

        // Load existing weights
        loadWeightsFromFirebase()

        // Show current date
        updateDateInView()

        // Open date picker
        binding.dateInput.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateInView()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save weight entry
        binding.saveWeightButton.setOnClickListener {
            Log.d("WeightDebug", "Save button clicked")

            val weightText = binding.weightInput.text.toString()
            val dateText = binding.dateInput.text.toString()

            Log.d("WeightDebug", "Weight: $weightText, Date: $dateText, DogId: $dogId")

            if (weightText.isBlank() || dateText.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightText.toDoubleOrNull()
            if (weight == null) {
                Toast.makeText(requireContext(), "Invalid weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val entry = WeightEntry(weight, dateText)

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                Log.e("WeightDebug", "FirebaseAuth.getInstance().currentUser is null")
                return@setOnClickListener
            }

            Log.d("WeightDebug", "Saving entry to Firestore: $entry")

            FirebaseFirestore.getInstance()
                .collection("users").document(user.uid)
                .collection("dogs").document(dogId)
                .collection("weights")
                .add(entry)
                .addOnSuccessListener {
                    adapter.addEntry(entry)
                    binding.weightInput.text?.clear()
                    Toast.makeText(requireContext(), "Weight saved", Toast.LENGTH_SHORT).show()
                    Log.d("WeightDebug", "Entry saved successfully to Firestore")

                    // Update latest weight for profile
                    FirebaseFirestore.getInstance()
                        .collection("users").document(user.uid)
                        .collection("dogs").document(dogId)
                        .update("latestWeight", weight)
                        .addOnSuccessListener {
                            Log.d("WeightDebug", "latestWeight updated successfully")
                        }
                        .addOnFailureListener {
                            Log.e("WeightDebug", "Failed to update latestWeight: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show()
                    Log.e("WeightDebug", "Failed to save to Firestore: ${it.message}")
                }
        }
    }

    private fun updateDateInView() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateInput.setText(format.format(calendar.time))
    }

    private fun loadWeightsFromFirebase() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(user.uid)
            .collection("dogs").document(dogId)
            .collection("weights")
            .get()
            .addOnSuccessListener { documents ->
                entries.clear()
                for (doc in documents) {
                    val entry = doc.toObject(WeightEntry::class.java)
                    entries.add(entry)
                }
                entries.sortByDescending { it.date }
                adapter.notifyDataSetChanged()
                Log.d("WeightDebug", "Loaded ${entries.size} weight entries from Firestore")
            }
            .addOnFailureListener {
                Log.e("WeightDebug", "Failed to load entries: ${it.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
