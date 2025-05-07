package com.example.pawsitivelife.ui.mydogs

import ActivityCareAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentDogProfileBinding


class DogProfileFragment : Fragment() {

    private var _binding: FragmentDogProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dog: Dog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Example dog  (after, needs to accept a parameters)
        dog = Dog(
            name = "Chubbie",
            breed = "Great Pyrenees Mix",
            dateOfBirth = "September 14 2021",
            color = "Golden",
            neutered = true,
            microchipped = true,
            imageResId = R.drawable.img_chubbie
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDogProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateDogData()
        setupActivityCareTracker()

        // Navigate to Edit Notes screen on Care Notes title click
        binding.profileLBLEditNotesClick.setOnClickListener {
            findNavController().navigate(R.id.action_dogProfileFragment_to_editNotesFragment)
        }

    }

    private fun populateDogData() {
        // Set dog image and name at the top
        binding.profileIMGDog.setImageResource(dog.imageResId)
        binding.profileLBLName.text = dog.name

        // Prepare list of information items
        val dogInfoItems = listOf(
            DogInfoItem("Breed", dog.breed),
            DogInfoItem("Date of Birth", dog.dateOfBirth),
            DogInfoItem("Color", dog.color),
            DogInfoItem("Neutered", if (dog.neutered) "Yes" else "No"),
            DogInfoItem("Microchipped", if (dog.microchipped) "Yes" else "No")
        )

        // Setup RecyclerView
        val adapter = DogInfoAdapter(dogInfoItems)
        binding.profileLSTInfo.layoutManager = LinearLayoutManager(requireContext())
        binding.profileLSTInfo.adapter = adapter
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
            when (item.title) {
                "Walk" -> findNavController().navigate(R.id.action_dogProfileFragment_to_walksFragment)
                "Medicine" -> findNavController().navigate(R.id.action_dogProfileFragment_to_medicationFragment)
                "Feed" -> findNavController().navigate(R.id.action_dogProfileFragment_to_feedingFragment)
                "Training" -> findNavController().navigate(R.id.action_dogProfileFragment_to_trainingFragment)
                "Weight" -> findNavController().navigate(R.id.action_dogProfileFragment_to_weightFragment)
            }
        }

        binding.profileLSTQuickActions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = activityCareAdapter
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
