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
            color = "Golden",
            neutered = true,
            microchipped = true,
            imageResId = 0 // Using 0 as default image
        ))
    }
} 