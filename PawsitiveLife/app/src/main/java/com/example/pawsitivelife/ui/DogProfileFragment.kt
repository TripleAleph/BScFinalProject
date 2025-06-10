package com.example.pawsitivelife.ui

import android.app.AlertDialog
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
import com.example.pawsitivelife.ui.mydogs.ActivityCareAdapter
import com.example.pawsitivelife.ui.mydogs.ActivityCareItem
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

    private lateinit var dogId: String

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

        dogId = arguments?.getString("dogId") ?: return
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

        setupActivityCareTracker()

        val user = FirebaseAuth.getInstance().currentUser ?: return
// Load notes and latest weight for the dog
        db.collection("users").document(user.uid)
            .collection("dogs").document(dogId)
            .get()
            .addOnSuccessListener { document ->
                // Load care notes
                val notes = document.getString("notes") ?: ""
                binding.profileLBLNotesText.text = notes

                // Load latest weight and display
                val latestWeight = document.getDouble("latestWeight")
                val weightText = if (latestWeight != null) "$latestWeight kg" else "-"
                binding.root.findViewById<TextView>(R.id.weight_value)?.text = weightText
            }
            .addOnFailureListener {
                // Handle failure
                binding.profileLBLNotesText.text = "Failed to load notes"
                binding.root.findViewById<TextView>(R.id.weight_value)?.text = "-"
            }


        binding.profileBTNEditImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.profileLBLEditNotesClick.setOnClickListener {
            val bundle = Bundle().apply {
                putString("dogId", dogId)
            }
            findNavController().navigate(R.id.action_dogProfileFragment_to_editNotesFragment, bundle)
        }

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

        // Delete Profile button click listener
        binding.dogProfileBTNDeleteProfile.setOnClickListener {
            showDeleteConfirmationDialog()
        }

    }

    // Confirmation dialog for deleting profile
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete this dog profile? All related data will be permanently removed.")
            .setPositiveButton("Delete") { _, _ ->
                deleteDogProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to delete dog document from Firestore
    private fun deleteDogProfile() {
        val user = auth.currentUser ?: return
        val dogRef = db.collection("users").document(user.uid).collection("dogs").document(dogId)

        //  All subcollections under the dog document
        val subcollections = listOf(
            "weights",
            "walks",
            "medication",
            "feeding",
            "training",
            "notes",
            "fixed_walk_times",
            "walk_log",
            "fixed_feed_times",
            "feed_log",
            "reminders"
        )

        // Prepare deletion tasks for each subcollection
        val deletionTasks = subcollections.map { sub ->
            dogRef.collection(sub).get().continueWithTask { snapshotTask ->
                val batch = db.batch()
                snapshotTask.result?.documents?.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
        }

        // After subcollections are deleted, delete the main dog document
        com.google.android.gms.tasks.Tasks.whenAllComplete(deletionTasks)
            .addOnSuccessListener {
                dogRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Dog profile deleted successfully 🐶", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to delete dog profile.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete dog-related data.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupActivityCareTracker() {
        val activityCareItems = listOf(
            ActivityCareItem(R.drawable.dog_walking, "Walk"),
            ActivityCareItem(R.drawable.ic_food, "Feed"),
            ActivityCareItem(R.drawable.ic_pills, "Medicine"),
            ActivityCareItem(R.drawable.ic_training, "Training"),
            ActivityCareItem(R.drawable.ic_weight, "Weight")
        )

        val activityCareAdapter = ActivityCareAdapter(activityCareItems) { item ->
            val bundle = Bundle().apply {
                putString("dogId", dogId)
            }

            when (item.title) {
                "Walk" -> findNavController().navigate(R.id.action_dogProfileFragment_to_walksFragment, bundle)
                "Medicine" -> findNavController().navigate(R.id.action_dogProfileFragment_to_medicationFragment, bundle)
                "Feed" -> findNavController().navigate(R.id.action_dogProfileFragment_to_feedingFragment, bundle)
                "Training" -> findNavController().navigate(R.id.action_dogProfileFragment_to_trainingFragment, bundle)
                "Weight" -> findNavController().navigate(R.id.action_dogProfileFragment_to_weightFragment, bundle)
            }
        }

        binding.profileLSTQuickActions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = activityCareAdapter
        }
    }

    private fun updateDogImageInFirestore(imagePath: String) {
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
