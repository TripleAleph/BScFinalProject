package com.example.pawsitivelife.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentDogParkBinding
import com.example.pawsitivelife.ui.mydogs.Dog

class DogParkFragment : Fragment() {

    private var _binding: FragmentDogParkBinding? = null
    private val binding get() = _binding!!

    private val dogArrivals = mutableListOf<DogArrival>() // List of dogs arriving at the park

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDogParkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        loadDefaultPark()
    }

    private fun setupRecyclerView() {
        val adapter = DogArrivalAdapter(dogArrivals) { dog ->
            showDogProfile(dog)
        }
        binding.dogParkRECYCLERArrivals.adapter = adapter
    }


    private fun setupButtons() {
        binding.dogParkBTNChange.setOnClickListener {
            // TODO: Open dialog to choose default park
        }

        binding.dogParkBTNFindNearby.setOnClickListener {
            // TODO: Open nearby parks list (bottom sheet or new screen)
        }
    }

    private fun loadDefaultPark() {
        // TODO: Load user's default park from SharedPreferences or Firestore
        binding.dogParkIMGPark.setImageResource(R.drawable.map_placeholder)
        // Example: binding.dogParkTXTParkName.text = "Marina Park"
    }

    private fun showDogProfile(dog: Dog) {
        // TODO: Open dog's detailed profile (fragment, dialog, or bottom sheet)
        Toast.makeText(requireContext(), "Clicked on ${dog.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
