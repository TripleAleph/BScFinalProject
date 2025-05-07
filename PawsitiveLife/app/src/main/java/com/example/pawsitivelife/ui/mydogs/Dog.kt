package com.example.pawsitivelife.ui.mydogs

data class Dog(
    val name: String,
    val breed: String,
    val dateOfBirth: String,
    val color: String,
    val neutered: Boolean,
    val microchipped: Boolean,
    val imageResId: Int
)
