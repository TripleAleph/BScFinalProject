package com.example.pawsitivelife.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.data.remote.DogApi
import com.example.pawsitivelife.databinding.FragmentAddDogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDogFragment : Fragment(R.layout.fragment_add_dog) {

    private var _binding: FragmentAddDogBinding? = null
    private val binding get() = _binding!!

    private var selectedGender: String? = null
    private var selectedImageUri: Uri? = null
    private var selectedBirthDate: String = ""

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.dogProfileImage.setImageURI(it)
        }
    }

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

        binding.dogProfileUploadIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.addDogTXTBirthdate.setOnClickListener {
            showDatePicker()
        }

        binding.addDogBTNSave.setOnClickListener {
            saveDogToFirestore()
        }
    }

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

    // Opens a date picker and stores the selected date
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val pickedDate = Calendar.getInstance()
                pickedDate.set(year, month, day)
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedBirthDate = formatter.format(pickedDate.time)
                binding.addDogTXTBirthdate.text = selectedBirthDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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

    // Saves dog data and uploads image if available
    private fun saveDogToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        val name = binding.addDogEDTName.text.toString().trim()
        val breed = binding.addDogAutoComp.text.toString().trim()
        val gender = selectedGender ?: ""
        val neutered = binding.addDogSWTSterilization.isChecked

        if (name.isEmpty() || breed.isEmpty() || gender.isEmpty() || selectedBirthDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            val imageRef = storage.reference.child("dogs/${user.uid}/${System.currentTimeMillis()}.jpg")
            imageRef.putFile(selectedImageUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                    imageRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    saveDogToFirestoreWithImage(uri.toString(), name, breed, selectedBirthDate, gender, neutered, user.uid, db)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveDogToFirestoreWithImage("", name, breed, selectedBirthDate, gender, neutered, user.uid, db)
        }
    }

    private fun saveDogToFirestoreWithImage(
        imageUrl: String,
        name: String,
        breed: String,
        birthDate: String,
        gender: String,
        neutered: Boolean,
        userId: String,
        db: FirebaseFirestore
    ) {
        val dog = hashMapOf(
            "name" to name,
            "breed" to breed,
            "dateOfBirth" to birthDate,
            "gender" to gender,
            "neutered" to neutered,
            "imageUrl" to imageUrl
        )


        db.collection("users")
            .document(userId)
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
