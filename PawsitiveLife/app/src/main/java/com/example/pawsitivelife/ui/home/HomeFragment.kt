package com.example.pawsitivelife.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentHomeBinding
import com.example.pawsitivelife.ui.mydogs.Dog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pawsitivelife.SignInActivity

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
        loadDogsFromFirestore()
        logAllDogsOfCurrentUser()
    }

    private fun loadDogsFromFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("DogLogger", "No user is currently logged in")
            Toast.makeText(
                requireContext(),
                "Please check your internet connection and try again",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigate(R.id.action_navigation_home_to_signInActivity)
            return
        }
        Log.d("DogLogger", "Loading dogs for user: ${user.uid}")

        db.collection("users").document(user.uid).collection("dogs")
            .get()
            .addOnSuccessListener { result ->
                Log.d("DogLogger", "Successfully loaded ${result.size()} dogs from Firestore")
                val dogList = result.map { document ->
                    Dog(
                        name = document.getString("name") ?: "",
                        breed = document.getString("breed") ?: "",
                        dateOfBirth = document.getString("dateOfBirth") ?: "-",
                        gender = document.getString("gender") ?: "",
                        color = document.getString("color") ?: "",
                        neutered = document.getBoolean("neutered") ?: false,
                        microchipped = document.getBoolean("microchipped") ?: false,
                        imageUrl = document.getString("imageUrl") ?: ""                    )
                }
                Log.d("DogLogger", "Mapped ${dogList.size} dogs to Dog objects")
                showDogs(dogList)
            }
            .addOnFailureListener { e ->
                Log.e("DogLogger", "Failed to load dogs from Firestore", e)
                Toast.makeText(
                    requireContext(),
                    "Network error: Please check your internet connection and try again",
                    Toast.LENGTH_LONG
                ).show()
                showDogs(emptyList())
            }
    }

    private fun showDogs(dogList: List<Dog>) {
        Log.d("DogLogger", "Showing ${dogList.size} dogs in RecyclerView")
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
        Log.d("DogLogger", "Adapter set on RecyclerView")
    }

    private fun logAllDogsOfCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid).collection("dogs")
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
                        val imageResId = document.getLong("imageResId")?.toInt() ?: R.drawable.img_chubbie

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
                                - imageResId: $imageResId
                            """.trimIndent()
                        )
                    }
                }
            }
            .addOnFailureListener {
                Log.e("DogLogger", "Failed to fetch dogs", it)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
