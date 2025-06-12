// File: app/src/main/java/com/example/pawsitivelife/ui/DogParkFragment.kt

package com.example.pawsitivelife.ui

import android.net.Uri
import java.io.File
import android.view.Gravity
import com.example.pawsitivelife.ui.mydogs.Dog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentDogParkBinding
import com.example.pawsitivelife.model.DogPark
import com.example.pawsitivelife.storage.DogParkRepository
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import kotlinx.coroutines.launch
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.CameraOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class DogParkFragment : Fragment() {
    private var _binding: FragmentDogParkBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private val dogParkRepository = DogParkRepository()
    private var selectedPark: DogPark? = null
    private var userLocation: Point? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableUserLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentDogParkBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.dogParkMAPView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
            enableUserLocation()
        }
        binding.dogVetBTNEdit.setOnClickListener {
            showVetSelectionDialog()
        }

        binding.dogPetStoreBTNEdit.setOnClickListener {
            showPetStoreSelectionDialog()
        }
        binding.btnSearchNearby.setOnClickListener { searchNearbyParks() }
        binding.btnImComing.setOnClickListener { announceArrival() }
        loadCurrentParkDogs()
        loadSelectedVet()
        loadSelectedPetStore()

    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
            mapView.location.addOnIndicatorPositionChangedListener { point ->
                userLocation = point
            }
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun searchNearbyParks() {
        if (userLocation == null) {
            Toast.makeText(requireContext(), "Waiting for location...", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val parks = dogParkRepository.searchNearbyParks(
                LatLng(userLocation!!.latitude(), userLocation!!.longitude()),
                5.0
            )
            showNearbyParksDialog(parks)
        }
    }

    private fun showNearbyParksDialog(parks: List<DogPark>) {
        val dialog = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_nearby_parks, null)
        val container = dialog.findViewById<LinearLayout>(R.id.parks_list_container)

        parks.forEach { park ->
            val parkView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_dog_park, container, false)

            parkView.findViewById<TextView>(R.id.park_name).text = park.name
            parkView.findViewById<TextView>(R.id.park_address).text = park.address

            parkView.setOnClickListener {
                selectedPark = park
                lifecycleScope.launch {
                    try {
                        dogParkRepository.setFavoritePark(park)
                        dogParkRepository.saveFavoritePark(park)
                        updateMapWithPark(park)
                        binding.dogParkTXTParkName.text = park.name
                        binding.dogParkTXTParkAddress.text = park.address
                        loadCurrentParkDogs()

                        Toast.makeText(
                            requireContext(),
                            "Park '${park.name}' saved as your favorite! 🐾",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error saving favorite park: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            container.addView(parkView)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nearby Dog Parks")
            .setView(dialog)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun announceArrival() {
        selectedPark?.let { park ->
            showDogSelectionDialog { selectedDogs ->
                lifecycleScope.launch {
                    selectedDogs.forEach { dog ->
                        dogParkRepository.announceDogArrival(park.id, dog)
                    }
                    loadCurrentParkDogs()
                    Toast.makeText(
                        requireContext(),
                        "You're coming to ${park.name}!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } ?: Toast.makeText(requireContext(), "Select a park first.", Toast.LENGTH_SHORT).show()
    }

    private fun loadCurrentParkDogs() {
        selectedPark?.let { park ->
            lifecycleScope.launch {
                val upcomingDogs = dogParkRepository.getUpcomingDogs(park.id)
                showUpcomingDogs(upcomingDogs)
            }
        }
    }


    private fun showDogSelectionDialog(onDogsSelected: (List<Dog>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("dogs")
            .get()
            .addOnSuccessListener { result ->
                val dogs = result.documents.mapNotNull { doc ->
                    doc.toObject(Dog::class.java)?.copy(dogId = doc.id)
                }

                if (dogs.isEmpty()) {
                    Toast.makeText(requireContext(), "You have no dogs registered.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val selected = BooleanArray(dogs.size)
                val dogNames = dogs.map { it.name.ifBlank { "Unnamed Dog" } }.toTypedArray()
                val selectedDogs = mutableListOf<Dog>()

                AlertDialog.Builder(requireContext())
                    .setTitle("Choose dogs coming to the park")
                    .setMultiChoiceItems(dogNames, selected) { _, which, isChecked ->
                        if (isChecked) selectedDogs.add(dogs[which])
                        else selectedDogs.remove(dogs[which])
                    }
                    .setPositiveButton("I'm Coming!") { _, _ ->
                        onDogsSelected(selectedDogs)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load your dogs: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun updateMapWithPark(park: DogPark) {
        pointAnnotationManager.deleteAll()
        val point = Point.fromLngLat(park.longitude, park.latitude)
        val marker = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage("marker-15")
            .withTextField(park.name)
        pointAnnotationManager.create(marker)

        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(point)
                .zoom(15.0)
                .build()
        )
    }

    private fun showUpcomingDogs(dogs: List<DogArrival>) {
        binding.layoutDogAvatars.removeAllViews()

        dogs.forEach { dogArrival ->
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
                gravity = Gravity.CENTER
            }

            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(120, 120)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(R.drawable.paw_logo) // fallback

                val imagePath = dogArrival.dog.imageUrl
                if (imagePath.isNotEmpty()) {
                    val file = File(imagePath)
                    if (file.exists()) {
                        setImageURI(Uri.fromFile(file))
                    }
                }
            }

            val nameView = TextView(requireContext()).apply {
                text = dogArrival.dog.name
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                textSize = 14f
            }

            container.addView(imageView)
            container.addView(nameView)
            binding.layoutDogAvatars.addView(container)
        }
    }

    private fun showVetSelectionDialog() {
        val db = FirebaseFirestore.getInstance()

        db.collection("vets")
            .get()
            .addOnSuccessListener { result ->
                val vets = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val address = doc.getString("address") ?: ""
                    val vetId = doc.id
                    Triple(name, address, vetId)
                }

                if (vets.isEmpty()) {
                    Toast.makeText(requireContext(), "No vets found.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val vetNames = vets.map { "${it.first} - ${it.second}" }.toTypedArray()

                AlertDialog.Builder(requireContext())
                    .setTitle("Choose Your Vet")
                    .setItems(vetNames) { _, which ->
                        val selectedVet = vets[which]
                        binding.dogVetTXTVetName.text = selectedVet.first
                        binding.dogVetTXTVetAddress.text = selectedVet.second

                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setItems
                        val vetData = mapOf(
                            "vetId" to selectedVet.third,
                            "name" to selectedVet.first,
                            "address" to selectedVet.second,
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(userId)
                            .collection("favorite_vet")
                            .document("selected")
                            .set(vetData)

                        Toast.makeText(requireContext(), "Vet selected: ${selectedVet.first}", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load vets: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showPetStoreSelectionDialog() {
        val db = FirebaseFirestore.getInstance()

        db.collection("petstores")
            .get()
            .addOnSuccessListener { result ->
                val stores = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val address = doc.getString("address") ?: ""
                    val storeId = doc.id
                    Triple(name, address, storeId)
                }

                if (stores.isEmpty()) {
                    Toast.makeText(requireContext(), "No pet stores found.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val storeNames = stores.map { "${it.first} - ${it.second}" }.toTypedArray()

                AlertDialog.Builder(requireContext())
                    .setTitle("Choose Your Pet Store")
                    .setItems(storeNames) { _, which ->
                        val selectedStore = stores[which]
                        binding.dogPetStoreTXTPetStoreName.text = selectedStore.first
                        binding.dogPetStoreTXTPetStoreAddress.text = selectedStore.second

                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setItems
                        val storeData = mapOf(
                            "storeId" to selectedStore.third,
                            "name" to selectedStore.first,
                            "address" to selectedStore.second,
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(userId)
                            .collection("favorite_petstore")
                            .document("selected")
                            .set(storeData)

                        Toast.makeText(requireContext(), "Pet store selected: ${selectedStore.first}", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load pet stores: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadSelectedVet() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("favorite_vet")
            .document("selected")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val address = doc.getString("address") ?: ""
                    binding.dogVetTXTVetName.text = name
                    binding.dogVetTXTVetAddress.text = address
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load vet info.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSelectedPetStore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("favorite_petstore")
            .document("selected")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val address = doc.getString("address") ?: ""
                    binding.dogPetStoreTXTPetStoreName.text = name
                    binding.dogPetStoreTXTPetStoreAddress.text = address
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load pet store info.", Toast.LENGTH_SHORT).show()
            }
    }




    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        _binding = null
    }
}
