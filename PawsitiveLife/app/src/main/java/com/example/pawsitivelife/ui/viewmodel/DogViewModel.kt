package com.example.pawsitivelife.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pawsitivelife.ui.mydogs.Dog

class DogViewModel : ViewModel() {
    private val _dogs = mutableListOf<Dog>()
    val dogs: List<Dog> get() = _dogs

    init {
        // TODO: Replace with actual data from repository
        _dogs.add(Dog(
            name = "Chubbie",
            breed = "Great Pyrenees Mix",
            dateOfBirth = "September 14 2021",
            gender = "Male",
            color = "Golden",
            neutered = true,
            microchipped = true,
            imageUrl = "" // Use a default image URL or empty string
        ))
    }
}
