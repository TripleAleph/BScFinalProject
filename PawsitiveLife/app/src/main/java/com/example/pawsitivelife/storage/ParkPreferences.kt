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

    // ===== Vet Preferences =====
    private const val KEY_VET_NAME = "vet_name"
    private const val KEY_VET_ADDRESS = "vet_address"

    fun saveVetDetails(context: Context, name: String, address: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString(KEY_VET_NAME, name)
            putString(KEY_VET_ADDRESS, address)
            apply()
        }
    }

    fun getVetName(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_VET_NAME, "My Vet") ?: "My Vet"
    }

    fun getVetAddress(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_VET_ADDRESS, "123 Clinic St, City") ?: "123 Clinic St, City"
    }


    // ===== Pet Store Preferences =====
    private const val KEY_STORE_NAME = "store_name"
    private const val KEY_STORE_ADDRESS = "store_address"

    fun savePetStoreDetails(context: Context, name: String, address: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString(KEY_STORE_NAME, name)
            putString(KEY_STORE_ADDRESS, address)
            apply()
        }
    }

    fun getPetStoreName(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_STORE_NAME, "My Pet Store") ?: "My Pet Store"
    }

    fun getPetStoreAddress(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_STORE_ADDRESS, "456 Pet Ave, City") ?: "456 Pet Ave, City"
    }

    fun saveVetPhone(context: Context, phone: String) {
        val prefs = context.getSharedPreferences("dog_park_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("vet_phone", phone).apply()
    }

    fun getVetPhone(context: Context): String {
        val prefs = context.getSharedPreferences("dog_park_prefs", Context.MODE_PRIVATE)
        return prefs.getString("vet_phone", "") ?: ""
    }

}
