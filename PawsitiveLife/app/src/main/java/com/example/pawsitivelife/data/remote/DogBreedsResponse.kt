package com.example.pawsitivelife.data.remote

data class DogBreedsResponse(
    val message: Map<String, List<String>>,
    val status: String
)
