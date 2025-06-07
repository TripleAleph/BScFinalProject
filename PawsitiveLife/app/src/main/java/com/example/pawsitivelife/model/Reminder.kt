package com.example.pawsitivelife.model

import java.time.LocalDateTime

data class Reminder(
    val title: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val dogId: String = "",
    val dogName: String = "",
    val imagePath: String? = null,
    val notes: String? = null

)
