package com.example.pawsitivelife.ui

import com.example.pawsitivelife.ui.mydogs.Dog


data class DogArrival(
    val dog: Dog,
    val arrivalTime: Long = System.currentTimeMillis()
)


