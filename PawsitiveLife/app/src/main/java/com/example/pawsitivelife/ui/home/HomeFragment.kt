package com.example.pawsitivelife.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentHomeBinding
import com.example.pawsitivelife.ui.mydogs.Dog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUsername()
        loadDogsFromFirestore()
        // Logs all dogs saved under the current user in Firestore for debugging purposes
        logAllDogsOfCurrentUser()
    }

    private fun loadUsername() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "unknown"
                binding.homeLBLUsername.text = "@$username"
            }
            .addOnFailureListener {
                binding.homeLBLUsername.text = "@error"
            }
    }

    private fun loadDogsFromFirestore() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid).collection("dogs")
            .get()
            .addOnSuccessListener { result ->
                val dogList = result.map { document ->
                    Dog(
                        name = document.getString("name") ?: "",
                        breed = document.getString("breed") ?: "",
                        dateOfBirth = document.getString("dateOfBirth") ?: "-",
                        gender = document.getString("gender") ?: "",
                        color = document.getString("color") ?: "",
                        neutered = document.getBoolean("neutered") ?: false,
                        microchipped = document.getBoolean("microchipped") ?: false,
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                }
                showDogs(dogList)
            }
            .addOnFailureListener {
                // Show empty list to ensure "Add Dog" card is visible
                showDogs(emptyList())
            }
    }

    private fun showDogs(dogList: List<Dog>) {
        val adapter = DogCardAdapter(
            dogs = dogList,
            onDogClick = { dog ->
                val bundle = Bundle().apply {
                    putString("name", dog.name)
                    putString("dateOfBirth", dog.dateOfBirth)
                    putString("gender", dog.gender)
                    putString("imageUrl", dog.imageUrl)
                    putString("breed", dog.breed)
                    putString("color", dog.color)
                    putBoolean("neutered", dog.neutered)
                    putBoolean("microchipped", dog.microchipped)
                }
                findNavController().navigate(R.id.action_navigation_home_to_dogProfileFragment, bundle)
            },
            onAddDogClick = {
                findNavController().navigate(R.id.action_navigation_home_to_addDogFragment)
            }
        )

        binding.homeLSTDogCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.homeLSTDogCards.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logAllDogsOfCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(user.uid)
            .collection("dogs")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("DogLogger", "No dogs found for user ${user.uid}")
                } else {
                    Log.d("DogLogger", "Dogs for user ${user.uid}:")
                    for (document in result) {
                        val name = document.getString("name") ?: "Unnamed"
                        val breed = document.getString("breed") ?: "Unknown"
                        val gender = document.getString("gender") ?: "-"
                        val birth = document.getString("dateOfBirth") ?: "-"
                        val color = document.getString("color") ?: "-"
                        val neutered = document.getBoolean("neutered") ?: false
                        val microchipped = document.getBoolean("microchipped") ?: false

                        Log.d(
                            "DogLogger", """
                            🐶 Dog:
                            - Name: $name
                            - Breed: $breed
                            - Gender: $gender
                            - Date of Birth: $birth
                            - Color: $color
                            - Neutered: $neutered
                            - Microchipped: $microchipped
                        """.trimIndent()
                        )
                    }
                }
            }
            .addOnFailureListener {
                Log.e("DogLogger", "Failed to fetch dogs", it)
            }
    }

}
