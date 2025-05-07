package com.example.pawsitivelife.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Reminder(
    val title: String,
    val date: LocalDateTime,
    val vetName: String,
    val imageResId: Int // או imageUrl: String אם את טוענת מרחוק
)
