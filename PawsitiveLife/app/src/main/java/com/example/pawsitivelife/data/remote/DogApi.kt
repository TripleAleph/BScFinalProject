package com.example.pawsitivelife.data.remote

import com.example.pawsitivelife.network.DogApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DogApi {
    private const val BASE_URL = "https://dog.ceo/api/"

    val retrofitService: DogApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogApiService::class.java)
    }
}