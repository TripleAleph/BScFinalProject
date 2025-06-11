package com.example.pawsitivelife.model

data class FeedLogEntry(
    val date: String = "",
    var completed: Boolean = false,
    var note: String = ""
)
