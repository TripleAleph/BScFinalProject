package com.example.pawsitivelife.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.data.remote.DogApi
import com.example.pawsitivelife.databinding.FragmentAddDogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddDogFragment : Fragment(R.layout.fragment_add_dog) {

    private var _binding: FragmentAddDogBinding? = null
    private val binding get() = _binding!!

    private var selectedGender: String? = null
    private var selectedImageUri: Uri? = null
    private var selectedBirthDate: String = ""

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
            checkAndRequestImagePermission()
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

    private fun saveDogToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        val name = binding.addDogEDTName.text.toString().trim()
        val breed = binding.addDogAutoComp.text.toString().trim()
        val gender = selectedGender ?: ""
        val color = binding.addDogEDTColor.text.toString().trim()
        val neutered = binding.addDogSWTSterilization.isChecked
        val microchipped = binding.addDogSWTMicrochipped.isChecked


        if (name.isEmpty() || breed.isEmpty() || gender.isEmpty() || selectedBirthDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val imagePath = selectedImageUri?.let { saveImageLocally(requireContext(), it) } ?: ""

        saveDogToFirestoreWithImage(
            imagePath,
            name, breed, selectedBirthDate,
            gender, color, neutered, microchipped, user.uid, db
        )
    }

    private fun saveImageLocally(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val fileName = "dog_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveDogToFirestoreWithImage(
        imagePath: String,
        name: String,
        breed: String,
        birthDate: String,
        gender: String,
        color: String,
        neutered: Boolean,
        microchipped: Boolean,
        userId: String,
        db: FirebaseFirestore
    ) {
        val dog = hashMapOf(
            "name" to name,
            "breed" to breed,
            "dateOfBirth" to birthDate,
            "gender" to gender,
            "color" to color,
            "neutered" to neutered,
            "microchipped" to microchipped,
            "imageUrl" to imagePath
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

    private fun checkAndRequestImagePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }

            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(requireContext(), "Permission is needed to select an image", Toast.LENGTH_SHORT).show()
            }

            else -> {
                requestPermissions(arrayOf(permission), IMAGE_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IMAGE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val IMAGE_PERMISSION_REQUEST_CODE = 101
    }
}