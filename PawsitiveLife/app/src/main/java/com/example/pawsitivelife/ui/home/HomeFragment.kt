package com.example.pawsitivelife.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pawsitivelife.ui.mydogs.Dog

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
    }

    // Load and display the current user's username
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

    // Fetch user's dogs from Firestore and display them in a horizontal RecyclerView
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
                        color = "",
                        neutered = document.getBoolean("neutered") ?: false,
                        microchipped = false,
                        imageUrl = document.getString("imageUrl") ?: ""
                    )

                }

                val adapter = DogCardAdapter(
                    dogs = dogList,
                    onDogClick = { dog ->
                        // Navigate to dog profile with dog data passed as arguments
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
                        // Navigate to Add Dog screen
                        findNavController().navigate(R.id.action_navigation_home_to_addDogFragment)
                    }
                )

                binding.homeLSTDogCards.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.homeLSTDogCards.adapter = adapter
            }
            .addOnFailureListener {
                // You can show an error message here if needed
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
