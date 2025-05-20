package com.example.pawsitivelife.ui.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.databinding.FragmentProfileSetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import com.example.pawsitivelife.R

class ProfileSetupFragment : Fragment() {

    private var _binding: FragmentProfileSetupBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

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
        _binding = FragmentProfileSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileImageUploadButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.saveProfileButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun saveUserProfile() {
        val username = binding.usernameEditText.text.toString().trim()
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val imagePath = selectedImageUri?.let {
            saveImageLocally(requireContext(), it)
        } ?: ""

        val userProfile = hashMapOf(
            "username" to username,
            "profileImageUrl" to imagePath
        )

        db.collection("users")
            .document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_profileSetupFragment_to_homeFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageLocally(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
