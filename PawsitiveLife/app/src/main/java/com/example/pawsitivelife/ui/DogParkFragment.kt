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
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.GoogleMap
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.Geocoder
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.widget.ImageView
import android.view.Gravity
import android.graphics.Color
import android.widget.CheckBox
import android.widget.EditText
import com.example.pawsitivelife.storage.ParkPreferences
import androidx.appcompat.app.AlertDialog
import com.example.pawsitivelife.storage.DogParkRepository
import com.example.pawsitivelife.storage.FakeDogParkRepository


class DogParkFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDogParkBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private lateinit var dogRepository: DogParkRepository


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDogParkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        loadDefaultPark()

        mapFragment = childFragmentManager.findFragmentById(R.id.dogPark_MAP_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        dogRepository = FakeDogParkRepository()

        dogRepository.getDogsInPark { updatedDogs ->
            showDogsInPark(updatedDogs)
        }

        addDogsToVet()
        addDogsToStoreShop()

    }

    private fun showDogsInPark(dogs: List<Dog>) {
        val dogAvatarsLayout = binding.layoutDogAvatars
        dogAvatarsLayout.removeAllViews()

        addDogViewsToLayout(dogs, dogAvatarsLayout)
    }

    private fun setupButtons() {
        binding.dogParkBTNEdit.setOnClickListener {
            showEditParkDialog()
        }

        binding.btnImComing.setOnClickListener {
            showDogSelectionDialog()
        }
    }

    private fun showDogSelectionDialog() {
        dogRepository.getAllDogs { allDogs ->
            dogRepository.getDogsInPark { currentParkDogs ->

                val dogsInParkNames = currentParkDogs.map { it.name }.toSet()
                val myAvailableDogs = allDogs.filter { it.isMine && it.name !in dogsInParkNames }

                val selectedDogs = mutableSetOf<String>()
                val dialogView = layoutInflater.inflate(R.layout.dialog_select_dogs, null)
                val container = dialogView.findViewById<LinearLayout>(R.id.dogs_list_container)

                myAvailableDogs.forEach { dog ->
                    val checkBox = CheckBox(requireContext()).apply {
                        text = dog.name
                        setPadding(8, 8, 8, 8)
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) selectedDogs.add(dog.name) else selectedDogs.remove(dog.name)
                        }
                    }
                    container.addView(checkBox)
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Select Dogs")
                    .setView(dialogView)
                    .setPositiveButton("Confirm") { _, _ ->
                        selectedDogs.forEach { dogName ->
                            val dog = allDogs.first { it.name == dogName && it.isMine }
                            dogRepository.addDogToPark(dog)
                        }

                        dogRepository.getDogsInPark { updated ->
                            showDogsInPark(updated)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

    }

    private fun addDogViewsToLayout(dogList: List<Dog>, container: LinearLayout) {
        for (dog in dogList) {
            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(16, 0, 16, 0) }
            }

            val image = ImageView(requireContext()).apply {
                setImageResource(R.drawable.paw_logo)

                layoutParams = LinearLayout.LayoutParams(120, 120)
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = if (dog.isMine) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.green_border)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.circular_border)
                }

                clipToOutline = true

                if (dog.isMine) {
                    setOnClickListener {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Remove Dog")
                            .setMessage("Are you sure you want to remove ${dog.name} from the park?")
                            .setPositiveButton("Yes") { _, _ ->
                                removeDogFromPark(dog.name)
                                Toast.makeText(requireContext(), "${dog.name} removed", Toast.LENGTH_SHORT).show()

                                dogRepository.getDogsInPark { updatedDogs ->
                                    showDogsInPark(updatedDogs)
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }

            val text = TextView(requireContext()).apply {
                this.text = dog.name
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 6 }
            }

            layout.addView(image)
            layout.addView(text)
            container.addView(layout)
        }
    }

    private fun removeDogFromPark(dogName: String) {
        dogRepository.getDogsInPark { allDogs ->
            val dogToRemove = allDogs.firstOrNull { it.name == dogName && it.isMine }
            if (dogToRemove != null) {
                dogRepository.removeDogFromPark(dogToRemove)
                dogRepository.getDogsInPark { updatedDogs ->
                    showDogsInPark(updatedDogs)
                }
            }
        }
    }


    private fun loadDefaultPark() {
        val name = ParkPreferences.getParkName(requireContext())
        val address = ParkPreferences.getParkAddress(requireContext())
        binding.dogParkTXTParkName.text = name
        binding.dogParkTXTParkAddress.text = address
    }

    private fun addDogsToVet() {
        val vetClientsLayout = binding.layoutDogClientsAvatars
        val dogsAtVet = listOf(
            Dog("Raichu", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Charlie", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Lola", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Luna", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Rain", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Boaz", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Mila", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Benny", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false)
        )
        addDogViewsToLayout(dogsAtVet, vetClientsLayout)
    }

    private fun addDogsToStoreShop() {
        val petStoreClientsLayout = binding.layoutDogStoreClientsAvatars
        val dogsAtPetStore = listOf(
            Dog("Raichu", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Luna", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Lola", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Mila", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false),
            Dog("Benny", "Mixed", "2020-01-01", "Brown", true, true, R.drawable.paw_logo, isMine = false)
        )
        addDogViewsToLayout(dogsAtPetStore, petStoreClientsLayout)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val dogParkAddress = "Derech Yitzhak Rabin 30, Gedera, Israel"

        // Geocode the address to LatLng
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocationName(dogParkAddress, 1)

                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)

                    withContext(Dispatchers.Main) {
                        // Show marker at the dog park location
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Dog Park")
                        )

                        // Move camera to the marker
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                        // Update park name and address in UI
                        binding.dogParkTXTParkName.text = "Gedera Dog Park"
                        binding.dogParkTXTParkAddress.text = dogParkAddress
                    }
                } else {
                    Log.e("MapError", "Address not found for: $dogParkAddress")
                }
            } catch (e: Exception) {
                Log.e("MapError", "Geocoding error: ${e.message}")
            }
        }
    }

    private fun showEditParkDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_park, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.edit_park_name)
        val addressInput = dialogView.findViewById<EditText>(R.id.edit_park_address)

        nameInput.setText(binding.dogParkTXTParkName.text)
        addressInput.setText(binding.dogParkTXTParkAddress.text)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Park Info")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newAddress = addressInput.text.toString().trim()

                if (newName.isEmpty() || newAddress.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                ParkPreferences.saveParkDetails(requireContext(), newName, newAddress)
                loadDefaultPark() // refresh UI
                Toast.makeText(requireContext(), "Park updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
