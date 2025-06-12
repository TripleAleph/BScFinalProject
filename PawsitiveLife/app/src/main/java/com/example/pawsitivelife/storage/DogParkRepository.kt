package com.example.pawsitivelife.storage

import android.util.Log
import com.example.pawsitivelife.model.DogPark
import com.example.pawsitivelife.ui.DogArrival
import com.example.pawsitivelife.ui.mydogs.Dog
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class DogParkRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun searchNearbyParks(userLocation: LatLng, radiusKm: Double): List<DogPark> {
        val snapshot = db.collection("dog_parks").get().await()
        val allParks = snapshot.documents.mapNotNull { doc ->
            doc.toObject(DogPark::class.java)?.copy(id = doc.id)
        }

        // 🔸 Filter parks by distance from user location
        return allParks.filter { park ->
            val parkLatLng = LatLng(park.latitude, park.longitude)
            val distance = haversine(userLocation, parkLatLng)
            distance <= radiusKm
        }
    }

    suspend fun setFavoritePark(park: DogPark) {
        db.collection("users")
            .document(currentUserId())
            .collection("favorites")
            .document("dog_park")
            .set(park)
            .await()
    }

    suspend fun saveFavoritePark(park: DogPark) {
        val userId = currentUserId()
        if (userId.isEmpty()) return

        val favoriteParkData = mapOf(
            "name" to park.name,
            "address" to park.address,
            "latitude" to park.latitude,
            "longitude" to park.longitude,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            db.collection("users")
                .document(userId)
                .collection("favorite_park")
                .document("selected")
                .set(favoriteParkData)
                .await()
            Log.d("DogParkRepository", "Favorite park saved successfully.")
        } catch (e: Exception) {
            Log.e("DogParkRepository", "Error saving favorite park", e)
        }
    }

    suspend fun announceDogArrival(parkId: String, dog: Dog) {
        if (dog.dogId.isBlank()) {
            Log.e("DogParkRepository", "Cannot announce arrival: dogId is blank")
            return
        }

        val arrival = mapOf(
            "dogId" to dog.dogId,
            "dogName" to dog.name,
            "imageUrl" to dog.imageUrl,
            "arrivalTime" to System.currentTimeMillis()
        )

        db.collection("dog_parks")
            .document(parkId)
            .collection("arrivals")
            .document(dog.dogId)
            .set(arrival)
            .await()
    }

    suspend fun getUpcomingDogs(parkId: String): List<DogArrival> {
        val snapshot = db.collection("dog_parks")
            .document(parkId)
            .collection("arrivals")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val name = doc.getString("dogName") ?: return@mapNotNull null
            val imageUrl = doc.getString("imageUrl") ?: ""
            val dogId = doc.getString("dogId") ?: return@mapNotNull null
            val arrivalTime = doc.getLong("arrivalTime") ?: 0L

            val dog = com.example.pawsitivelife.ui.mydogs.Dog(
                name = name,
                imageUrl = imageUrl,
                dogId = dogId
            )
            DogArrival(dog, arrivalTime)
        }
    }

    private fun currentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    // 🔸 Calculate distance in kilometers between two LatLng points
    private fun haversine(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371.0 // Radius of the Earth in km

        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)

        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}
