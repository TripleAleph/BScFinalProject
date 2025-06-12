package com.example.pawsitivelife.model

data class DogPark(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isFavorite: Boolean = false,
    val currentDogs: List<String> = listOf(), // List of dog IDs currently in the park
    val upcomingDogs: Map<String, List<String>> = mapOf() // Map of timestamp to list of dog IDs planning to arrive
) 