package com.example.pawsitivelife.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentDogProfileBinding
import com.example.pawsitivelife.ui.mydogs.DogInfoAdapter
import com.example.pawsitivelife.ui.mydogs.DogInfoItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

class DogProfileFragment : Fragment() {

    private var _binding: FragmentDogProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val savedPath = saveImageLocally(requireContext(), it)
                if (savedPath != null) {
                    Glide.with(this)
                        .load(savedPath)
                        .placeholder(R.drawable.missing_img_dog)
                        .centerCrop()
                        .into(binding.profileIMGDog)

                    updateDogImageInFirestore(savedPath)
                } else {
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDogProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dogId = arguments?.getString("dogId") ?: return
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

        binding.root.findViewById<TextView>(R.id.age_value)?.text = calculateDogAge(birthDate)
        binding.root.findViewById<TextView>(R.id.gender_value)?.text = gender

        val infoList = listOf(
            DogInfoItem("Date of Birth", formatBirthDate(birthDate)),
            DogInfoItem("Breed", breed),
            DogInfoItem("Color", color),
            DogInfoItem("Neutered", if (neutered) "Yes" else "No"),
            DogInfoItem("Microchipped", if (microchipped) "Yes" else "No")
        )
        binding.profileLSTInfo.layoutManager = LinearLayoutManager(requireContext())
        binding.profileLSTInfo.adapter = DogInfoAdapter(infoList)

        // Load notes from Firestore
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid).collection("dogs").document(dogId)
            .get()
            .addOnSuccessListener { document ->
                val notes = document.getString("notes") ?: ""
                binding.profileLBLNotesText.text = notes
            }
            .addOnFailureListener {
                binding.profileLBLNotesText.text = "Failed to load notes"
            }

        // Edit image button
        binding.profileBTNEditImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Navigate to edit notes
        binding.profileLBLEditNotesClick.setOnClickListener {
            val bundle = Bundle().apply {
                putString("dogId", dogId)
            }
            findNavController().navigate(R.id.action_dogProfileFragment_to_editNotesFragment, bundle)
        }

        // Navigate to edit profile
        binding.dogProfileBTNEditProfile.setOnClickListener {
            val bundle = Bundle().apply {
                putString("dogId", dogId)
                putString("name", name)
                putString("dateOfBirth", birthDate)
                putString("gender", gender)
                putString("breed", breed)
                putString("color", color)
                putBoolean("neutered", neutered)
                putBoolean("microchipped", microchipped)
            }
            findNavController().navigate(R.id.action_dogProfileFragment_to_editDogProfileFragment, bundle)
        }
    }

    private fun updateDogImageInFirestore(imagePath: String) {
        val dogId = arguments?.getString("dogId") ?: return
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .collection("dogs").document(dogId)
            .update("imageUrl", imagePath)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Dog image updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update image", Toast.LENGTH_SHORT).show()
            }
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
            when {
                age.years > 0 && age.months > 0 -> "${age.years} years and ${age.months} months"
                age.years > 0 -> "${age.years} years"
                age.months > 0 -> "${age.months} months"
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
