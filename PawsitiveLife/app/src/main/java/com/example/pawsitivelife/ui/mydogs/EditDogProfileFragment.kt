package com.example.pawsitivelife.ui.mydogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.data.remote.DogApi
import com.example.pawsitivelife.databinding.FragmentEditDogProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch



class EditDogProfileFragment : Fragment() {

    private var _binding: FragmentEditDogProfileBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var dogId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDogProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dogId = arguments?.getString("dogId")
        val name = arguments?.getString("name") ?: ""
        val birthDate = arguments?.getString("dateOfBirth") ?: ""
        val gender = arguments?.getString("gender") ?: ""
        val breed = arguments?.getString("breed") ?: ""
        val color = arguments?.getString("color") ?: ""
        val neutered = arguments?.getBoolean("neutered") ?: false
        val microchipped = arguments?.getBoolean("microchipped") ?: false

        binding.editDogEDTName.setText(name)
        binding.editDogEDTDate.text = birthDate
        binding.editDogEDTColor.setText(color)
        binding.editDogSWNeutered.isChecked = neutered
        binding.editDogSWMicrochipped.isChecked = microchipped

        if (gender == "Male") binding.editDogGenderMale.isChecked = true
        else if (gender == "Female") binding.editDogGenderFemale.isChecked = true

        fetchBreedsFromApi(breed)

        binding.editDogEDTDate.setOnClickListener {
            showDatePicker()
        }

        binding.editDogBTNSave.setOnClickListener {
            saveChangesToFirestore()
        }
    }


    private fun fetchBreedsFromApi(currentBreed: String) {
        lifecycleScope.launch {
            try {
                val response = DogApi.retrofitService.getBreeds()
                if (response.isSuccessful) {
                    val breedsMap = response.body()?.message ?: emptyMap()
                    val breedList = breedsMap.keys
                        .map { it.replaceFirstChar { c -> c.uppercaseChar() } }
                        .sorted()

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        breedList
                    )
                    binding.editDogEDTBreed.setAdapter(adapter)
                    binding.editDogEDTBreed.setText(currentBreed, false)
                } else {
                    Toast.makeText(requireContext(), "Failed to load breeds", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Breed API error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val pickedDate = Calendar.getInstance()
                pickedDate.set(year, month, day)
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.editDogEDTDate.text = formatter.format(pickedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveChangesToFirestore() {
        val name = binding.editDogEDTName.text.toString()
        val breed = binding.editDogEDTBreed.text.toString()
        val dateOfBirth = binding.editDogEDTDate.text.toString()
        val color = binding.editDogEDTColor.text.toString()
        val gender = when {
            binding.editDogGenderMale.isChecked -> "Male"
            binding.editDogGenderFemale.isChecked -> "Female"
            else -> ""
        }
        val neutered = binding.editDogSWNeutered.isChecked
        val microchipped = binding.editDogSWMicrochipped.isChecked

        val user = auth.currentUser ?: return
        val docId = dogId ?: return

        val dogRef = db.collection("users").document(user.uid).collection("dogs").document(docId)
        val updatedData = mapOf(
            "name" to name,
            "breed" to breed,
            "dateOfBirth" to dateOfBirth,
            "gender" to gender,
            "color" to color,
            "neutered" to neutered,
            "microchipped" to microchipped
        )

        dogRef.update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Dog profile updated", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update dog", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
