package com.example.pawsitivelife.network

import retrofit2.Response
import retrofit2.http.GET

// This interface defines the Dog API endpoints
interface DogApiService {
    @GET("breeds/list/all")
    suspend fun getBreeds(): Response<BreedResponse>
}

// Data class to hold the response structure
data class BreedResponse(
    val status: String,
    val message: Map<String, List<String>>
)
