package com.example.pawsitivelife.model

import com.google.firebase.firestore.PropertyName

data class Vet(
    //@get:PropertyName("name")
    //@set:PropertyName("name")
    var name: String = "",

    //@get:PropertyName("address")
    //@set:PropertyName("address")
    var address: String = "",

    //@get:PropertyName("phone")
    //@set:PropertyName("phone")
    var phone: String = "",

    //@get:PropertyName("lat")
    //@set:PropertyName("lat")
    var lat: Double = 0.0,

    //@get:PropertyName("lng")
    //@set:PropertyName("lng")
    var lng: Double = 0.0
)
