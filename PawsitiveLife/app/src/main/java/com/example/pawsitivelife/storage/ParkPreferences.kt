package com.example.pawsitivelife.storage

import android.content.Context

object ParkPreferences {
    private const val PREF_NAME = "park_preferences"
    private const val KEY_PARK_NAME = "park_name"
    private const val KEY_PARK_ADDRESS = "park_address"
    private const val KEY_DOGS_IN_PARK = "dogs_in_park"

    fun saveParkDetails(context: Context, name: String, address: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString(KEY_PARK_NAME, name)
            putString(KEY_PARK_ADDRESS, address)
            apply()
        }
    }

    fun getParkName(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_PARK_NAME, "Gedera Dog Park") ?: "Gedera Dog Park"
    }

    fun getParkAddress(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_PARK_ADDRESS, "Derech Yitzhak Rabin 30, Gedera, Israel")
            ?: "Derech Yitzhak Rabin 30, Gedera, Israel"
    }

    fun saveDogsInPark(context: Context, dogs: List<String>) {
        val joined = dogs.joinToString(",")
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DOGS_IN_PARK, joined)
            .apply()
    }

    fun getDogsInPark(context: Context): List<String> {
        val joined = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DOGS_IN_PARK, "") ?: ""
        return if (joined.isEmpty()) emptyList() else joined.split(",")
    }
}
