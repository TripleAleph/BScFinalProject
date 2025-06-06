package com.example.pawsitivelife.model

data class WalkLogEntry(
    val date: String = "", // yyyy-MM-dd
    var completed: Boolean = false,
    var note: String = ""
)
