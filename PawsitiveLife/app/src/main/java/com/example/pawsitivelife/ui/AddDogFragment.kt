package com.example.pawsitivelife.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.data.remote.DogApi
import com.example.pawsitivelife.databinding.FragmentAddDogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class AddDogFragment : Fragment(R.layout.fragment_add_dog) {

    private var _binding: FragmentAddDogBinding? = null
    private val binding get() = _binding!!

    private var selectedGender: String? = null // Holds the selected gender ("Male" or "Female")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchBreedsFromApi()

        binding.maleButton.setOnClickListener { selectGender("Male") }
        binding.femaleButton.setOnClickListener { selectGender("Female") }

        binding.addDogBTNSave.setOnClickListener { saveDogToFirestore() }
    }

    // Fetch breed list from Dog API and set it to the dropdown
    private fun fetchBreedsFromApi() {
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
                    binding.addDogAutoComp.setAdapter(adapter)
                } else {
                    Toast.makeText(requireContext(), "Failed to load breeds", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Breed API error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Function to manage UI changes when selecting gender
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

    // Save dog details to Firestore
    private fun saveDogToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        val name = binding.addDogEDTName.text.toString().trim()
        val breed = binding.addDogAutoComp.text.toString().trim()
        val age = binding.dogAgeText.text.toString().trim()
        val gender = selectedGender ?: ""
        val neutered = binding.addDogSWTSterilization.isChecked
        val imageResId = R.drawable.img_chubbie // Temporary placeholder image

        if (name.isEmpty() || breed.isEmpty() || gender.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val dog = hashMapOf(
            "name" to name,
            "breed" to breed,
            "age" to age,
            "gender" to gender,
            "neutered" to neutered,
            "imageResId" to imageResId
        )

        db.collection("users")
            .document(user.uid)
            .collection("dogs")
            .add(dog)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Dog saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save dog", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
