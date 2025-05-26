package com.example.pawsitivelife.storage

import com.example.pawsitivelife.ui.mydogs.Dog

interface DogParkRepository {
    fun getDogsInPark(callback: (List<Dog>) -> Unit)
    fun addDogToPark(dog: Dog)
    fun removeDogFromPark(dog: Dog)
    fun getAllDogs(callback: (List<Dog>) -> Unit)

}
