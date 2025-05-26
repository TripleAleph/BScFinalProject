package com.example.pawsitivelife.storage

import com.example.pawsitivelife.ui.mydogs.Dog

class FakeDogParkRepository : DogParkRepository {

    private val allDogs = listOf(
        Dog("Raichu", "Vizsla", "2/2/24", "Caramel & White", true, true, 0, isMine = true),
        Dog("Chubbie", "Mixed", "3/2/24", "White", true, true, 0, isMine = true),
        Dog("Sky", "Mixed", "3/2/24", "White", true, true, 0, isMine = true),
        Dog("Bella", "Mixed", "4/2/24", "Brown", true, true, 0, isMine = false),
        Dog("Charlie", "Golden Retriever", "5/2/24", "Caramel", true, true, 0, isMine = false),
        Dog("Max", "Labrador", "3/2/24", "White", true, true, 0, isMine = false),
        Dog("Luna", "German Shepherd", "4/2/24", "Brown", true, true, 0, isMine = false),
        Dog("Panda", "Mixed", "5/2/24", "Caramel", true, true, 0, isMine = false),
        Dog("Rex", "Siberian Husky", "3/2/24", "White", true, true, 0, isMine = false)
    )

    private val dogsInPark = mutableListOf<Dog>().apply {
        addAll(allDogs.filter { !it.isMine })
    }

    override fun getDogsInPark(callback: (List<Dog>) -> Unit) {
        callback(dogsInPark.toList())
    }

    override fun addDogToPark(dog: Dog) {
        if (dogsInPark.none { it.name == dog.name }) {
            dogsInPark.add(dog)
        }
    }

    override fun removeDogFromPark(dog: Dog) {
        dogsInPark.removeIf { it.name == dog.name }
    }

    override fun getAllDogs(callback: (List<Dog>) -> Unit) {
        callback(allDogs)
    }
}
