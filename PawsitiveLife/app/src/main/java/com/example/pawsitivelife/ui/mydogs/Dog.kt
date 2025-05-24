package com.example.pawsitivelife.ui.mydogs

data class Dog(
    val name: String = "",
    val breed: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val color: String = "",
    val neutered: Boolean = false,
    val microchipped: Boolean = false,
    val imageUrl: String,
    val dogId: String = "",
    val isMine: Boolean
)
