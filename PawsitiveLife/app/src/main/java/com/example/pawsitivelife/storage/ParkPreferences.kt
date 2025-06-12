// File: app/src/main/java/com/example/pawsitivelife/storage/ParkPreferences.kt

package com.example.pawsitivelife.storage

import android.content.Context

/**
 * Manages shared-prefs for park info.
 */
object ParkPreferences {
    private const val PREF_NAME = "park_preferences"
    private const val KEY_PARK_NAME = "park_name"
    private const val KEY_PARK_ADDRESS = "park_address"

    fun saveParkDetails(context: Context, name: String, address: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PARK_NAME, name)
            .putString(KEY_PARK_ADDRESS, address)
            .apply()
    }

    fun getParkName(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PARK_NAME, "Gedera Dog Park")!!

    fun getParkAddress(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PARK_ADDRESS, "Derech Yitzhak Rabin 30, Gedera, Israel")!!

    /**
     * Returns a Firestore-safe key for this park (e.g. replace spaces).
     * We include UID in the key to avoid collisions if users name parks the same.
     */
    fun getParkKey(context: Context): String {
        val nameKey = getParkName(context).replace(Regex("\\s+"), "_")
        // optional: include userId if you want one-park-per-user isolation
        return nameKey
    }
}

// Extension to access Context from FirebaseUser
private fun com.google.firebase.auth.FirebaseUser.uidContext(): Context {
    throw IllegalStateException("Pass Context explicitly to getParkKey()")
}
