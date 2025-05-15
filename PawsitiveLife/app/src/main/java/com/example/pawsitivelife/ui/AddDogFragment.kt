package com.example.pawsitivelife.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentAddDogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        // Handle gender selection buttons
        binding.maleButton.setOnClickListener { selectGender("Male") }
        binding.femaleButton.setOnClickListener { selectGender("Female") }

        // Save dog data when clicking the save button
        binding.addDogBTNSave.setOnClickListener { saveDogToFirestore() }
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

    // Saves dog details to Firestore under the current user's document
    private fun saveDogToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        val name = binding.addDogEDTName.text.toString().trim()
        val breed = binding.addDogAutoComp.text.toString().trim()
        val age = binding.dogAgeText.text.toString().trim()
        val gender = selectedGender ?: ""
        val neutered = binding.addDogSWTSterilization.isChecked
        val imageResId = R.drawable.img_chubbie // Temporary placeholder image

        // Validate required fields
        if (name.isEmpty() || breed.isEmpty() || gender.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Construct a hash map representing the dog's data
        val dog = hashMapOf(
            "name" to name,
            "breed" to breed,
            "age" to age,
            "gender" to gender,
            "neutered" to neutered,
            "imageResId" to imageResId
        )

        // Save the dog under the current user's Firestore document
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
