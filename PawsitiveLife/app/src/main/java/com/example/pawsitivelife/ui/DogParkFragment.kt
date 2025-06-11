package com.example.pawsitivelife.ui

import android.content.Context
import android.content.Intent
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
import com.example.pawsitivelife.model.Vet
import com.example.pawsitivelife.storage.DogParkRepository
import com.example.pawsitivelife.storage.FakeDogParkRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import android.net.Uri


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

        dogRepository = FakeDogParkRepository()
        setupButtons()
        loadDefaultPark()
        loadDefaultVet()
        loadDefaultPetStore()

        addDogsToVet()
        addDogsToPetStore()


        mapFragment = childFragmentManager.findFragmentById(R.id.dogPark_MAP_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        dogRepository.getDogsInPark { updatedDogs ->
            showDogsInPark(updatedDogs)
        }

    }

    private fun setupButtons() {
        binding.dogParkBTNEdit.setOnClickListener {
            showEditParkDialog()
        }

        binding.btnImComing.setOnClickListener {
            showDogSelectionDialog()
        }

        binding.dogVetBTNEdit.setOnClickListener {
            showSelectVetDialog()
        }

        binding.dogPetStoreBTNEdit.setOnClickListener {
            showEditPetStoreDialog()
        }

        binding.btnCallNow.setOnClickListener {
            callVet()
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

                setOnClickListener {
                    when (container.id) {
                        R.id.layoutDogAvatars -> {
                            if (dog.isMine) {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Remove Dog")
                                    .setMessage("Remove ${dog.name} from the park?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        removeDogFromPark(dog.name)
                                        dogRepository.getDogsInPark { updatedDogs ->
                                            showDogsInPark(updatedDogs)
                                        }
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }
                        }

                        R.id.layoutDogClientsAvatars -> {
                            if (dog.isMine) {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Remove Dog")
                                    .setMessage("Remove ${dog.name} from this vet?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        val selectedVetName = ParkPreferences.getVetName(requireContext())
                                        val sharedPrefKey = generateVetKey(selectedVetName)
                                        val sharedPref = requireContext().getSharedPreferences("VetPrefs", Context.MODE_PRIVATE)
                                        val currentSet = sharedPref.getStringSet(sharedPrefKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                                        currentSet.remove(dog.name)
                                        sharedPref.edit().putStringSet(sharedPrefKey, currentSet).apply()
                                        addDogsToVet()
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }
                        }

                        R.id.layoutDogStoreClientsAvatars -> {
                            if (dog.isMine) {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Remove Dog")
                                    .setMessage("Remove ${dog.name} from this store?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        val selectedStoreName = ParkPreferences.getPetStoreName(requireContext())
                                        val sharedPrefKey = generateStoreKey(selectedStoreName)
                                        val sharedPref = requireContext().getSharedPreferences("StorePrefs", Context.MODE_PRIVATE)
                                        val currentSet = sharedPref.getStringSet(sharedPrefKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                                        currentSet.remove(dog.name)
                                        sharedPref.edit().putStringSet(sharedPrefKey, currentSet).apply()
                                        addDogsToPetStore()
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }
                        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*------------------------------------------- PARK -------------------------------------------*/

    private fun showDogsInPark(dogs: List<Dog>) {
        val dogAvatarsLayout = binding.layoutDogAvatars
        dogAvatarsLayout.removeAllViews()

        addDogViewsToLayout(dogs, dogAvatarsLayout)
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

    /*------------------------------------------- VET -------------------------------------------*/

    private fun loadDefaultVet() {
        val name = ParkPreferences.getVetName(requireContext())
        val address = ParkPreferences.getVetAddress(requireContext())

        if (name.isBlank()) {
            binding.dogVetTXTVetName.text = "No vet selected"
            binding.dogVetTXTVetAddress.text = "-"
        } else {
            binding.dogVetTXTVetName.text = name
            binding.dogVetTXTVetAddress.text = address
        }
    }

    private fun showSelectVetDialog() {
        val vetList = listOf(
            Vet("", "", "", 0.0, 0.0),
            Vet("Gedera Vet Clinic", "Habanim 23, Gedera", "081234567", 31.8087, 34.7643),
            Vet("Tel Aviv Vet Center", "Dizengoff 100, Tel Aviv", "039876543", 32.0853, 34.7818),
            Vet("Jerusalem Vet", "King George 5, Jerusalem", "029999999", 31.7683, 35.2137)
        )

        //val vetNames = vetList.map { it.name }.toTypedArray()
        val vetNames = listOf("No vet selected", "Gedera Vet Clinic", "Tel Aviv Vet Center", "Jerusalem Vet")

        AlertDialog.Builder(requireContext())
            .setTitle("Select a Vet")
            .setItems(vetNames.toTypedArray()) { _, which ->
                val selectedVet = vetList[which]
                if (selectedVet.name.isBlank()) {
                    ParkPreferences.saveVetDetails(requireContext(), "", "")
                    ParkPreferences.saveVetPhone(requireContext(), "")
                } else {
                    ParkPreferences.saveVetDetails(requireContext(), selectedVet.name, selectedVet.address)
                    ParkPreferences.saveVetPhone(requireContext(), selectedVet.phone)
                    showVetDogSelectionDialog(selectedVet.name)
                }

                loadDefaultVet()
                addDogsToVet()
            }

            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVetDogSelectionDialog(vetName: String) {
        dogRepository.getAllDogs { allDogs ->
            val myDogs = allDogs.filter { it.isMine }

            val allVetPrefs = requireContext().getSharedPreferences("VetPrefs", Context.MODE_PRIVATE).all
            val alreadyAssignedDogs = mutableSetOf<String>()
            for ((_, value) in allVetPrefs) {
                if (value is Set<*>) {
                    @Suppress("UNCHECKED_CAST")
                    alreadyAssignedDogs.addAll(value as Set<String>)
                }
            }

            val availableDogs = myDogs.filter { it.name !in alreadyAssignedDogs }

            val selectedDogs = mutableSetOf<String>()
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_dogs, null)
            val container = dialogView.findViewById<LinearLayout>(R.id.dogs_list_container)

            availableDogs.forEach { dog ->
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
                .setTitle("Which of your dogs go to this vet?")
                .setView(dialogView)
                .setPositiveButton("Confirm") { _, _ ->
                    val sharedPrefKey = generateVetKey(vetName)
                    val sharedPref = requireContext().getSharedPreferences("VetPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putStringSet(sharedPrefKey, selectedDogs).apply()
                    addDogsToVet()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun generateVetKey(vetName: String): String {
        return "vet_dogs_" + vetName.replace(Regex("[^A-Za-z0-9]"), "_")
    }

    private fun callVet() {
        val phone = ParkPreferences.getVetPhone(requireContext())
        if (phone.isBlank()) {
            Toast.makeText(requireContext(), "No vet selected", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }

        startActivity(intent)
    }

    private fun addDogsToVet() {
        val vetClientsLayout = binding.layoutDogClientsAvatars
        vetClientsLayout.removeAllViews()

        val selectedVetName = ParkPreferences.getVetName(requireContext())
        val sharedPrefKey = generateVetKey(selectedVetName)

        val sharedPref = requireContext().getSharedPreferences("VetPrefs", Context.MODE_PRIVATE)
        val userDogNames = sharedPref.getStringSet(sharedPrefKey, emptySet()) ?: emptySet()

        dogRepository.getAllDogs { allDogs ->
            val myDogsAtVet = allDogs.filter { dog -> dog.name in userDogNames }

            val fixedDogs = when (selectedVetName) {
                "Gedera Vet Clinic" -> listOf(Dog("Max", "Bulldog", "Male", "2020-01-01", "Brown", true, true, "","", R.drawable.paw_logo,false))
                "Tel Aviv Vet Center" -> listOf(Dog("Luna", "Husky", "Female", "2021-03-03", "Grey", true, true, "", "", R.drawable.paw_logo, false))
                "Jerusalem Vet" -> listOf(Dog("Coco", "Shih Tzu", "Female", "2018-07-15", "White", false, true, "","", R.drawable.paw_logo, false))
                else -> emptyList()
            }

            val allDogsAtVet = fixedDogs + myDogsAtVet
            addDogViewsToLayout(allDogsAtVet, vetClientsLayout)
        }
    }

    /*------------------------------------------- PET STORE -------------------------------------------*/

    private fun loadDefaultPetStore() {
        val name = ParkPreferences.getPetStoreName(requireContext())
        val address = ParkPreferences.getPetStoreAddress(requireContext())
        binding.dogPetStoreTXTPetStoreName.text = name
        binding.dogPetStoreTXTPetStoreAddress.text = address
    }

    private fun showEditPetStoreDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_park, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.edit_park_name)
        val addressInput = dialogView.findViewById<EditText>(R.id.edit_park_address)

        nameInput.setText(binding.dogPetStoreTXTPetStoreName.text)
        addressInput.setText(binding.dogPetStoreTXTPetStoreAddress.text)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Store Info")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newAddress = addressInput.text.toString().trim()

                if (newName.isEmpty() || newAddress.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                ParkPreferences.savePetStoreDetails(requireContext(), newName, newAddress)
                loadDefaultPetStore()
                showPetStoreDogSelectionDialog(newName)

                Toast.makeText(requireContext(), "Store updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addDogsToPetStore() {
        val storeClientsLayout = binding.layoutDogStoreClientsAvatars
        storeClientsLayout.removeAllViews()

        val selectedStoreName = ParkPreferences.getPetStoreName(requireContext())
        val sharedPrefKey = generateStoreKey(selectedStoreName)
        val sharedPref = requireContext().getSharedPreferences("StorePrefs", Context.MODE_PRIVATE)
        val userDogNames = sharedPref.getStringSet(sharedPrefKey, emptySet()) ?: emptySet()

        dogRepository.getAllDogs { allDogs ->
            val myDogsAtStore = allDogs.filter { it.name in userDogNames }

            val fixedDogs = when (selectedStoreName) {
                "Pet Planet Gedera" -> listOf(Dog("Bella", "Poodle", "Female", "2019-06-15", "White", false, true, "", "", R.drawable.paw_logo, false))
                "HaTzanua Store TA" -> listOf(Dog("Rex", "Labrador", "Male", "2018-11-10", "Yellow", true, false, "", "", R.drawable.paw_logo, false))
                else -> emptyList()
            }

            val allDogsAtStore = fixedDogs + myDogsAtStore
            addDogViewsToLayout(allDogsAtStore, storeClientsLayout)
        }
    }

    private fun generateStoreKey(storeName: String): String {
        return "store_dogs_" + storeName.replace(Regex("[^A-Za-z0-9]"), "_")
    }

    private fun showPetStoreDogSelectionDialog(storeName: String) {
        dogRepository.getAllDogs { allDogs ->
            val myDogs = allDogs.filter { it.isMine }

            val allStorePrefs = requireContext().getSharedPreferences("StorePrefs", Context.MODE_PRIVATE).all
            val alreadyAssignedDogs = mutableSetOf<String>()
            for ((_, value) in allStorePrefs) {
                if (value is Set<*>) {
                    @Suppress("UNCHECKED_CAST")
                    alreadyAssignedDogs.addAll(value as Set<String>)
                }
            }

            val availableDogs = myDogs.filter { it.name !in alreadyAssignedDogs }

            val selectedDogs = mutableSetOf<String>()
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_dogs, null)
            val container = dialogView.findViewById<LinearLayout>(R.id.dogs_list_container)

            availableDogs.forEach { dog ->
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
                .setTitle("Which of your dogs go to this store?")
                .setView(dialogView)
                .setPositiveButton("Confirm") { _, _ ->
                    val sharedPrefKey = generateStoreKey(storeName)
                    val sharedPref = requireContext().getSharedPreferences("StorePrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putStringSet(sharedPrefKey, selectedDogs).apply()
                    addDogsToPetStore()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

}