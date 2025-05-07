package com.example.pawsitivelife.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.databinding.FragmentHomeBinding
import com.example.pawsitivelife.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load username from Firestore
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("username")) {
                        val username = document.getString("username")
                        binding.homeLBLUsername.text = "@$username"
                    } else {
                        binding.homeLBLUsername.text = "@unknown"
                    }
                }
                .addOnFailureListener {
                    binding.homeLBLUsername.text = "@error"
                }
        }

        binding.homeCARDAddDog.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_addDogFragment)
        }

        binding.homeCARDDogCard.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_dogProfileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
