package com.example.pawsitivelife.model

import java.time.LocalTime

data class FixedWalkTime(
    val type: WalkType = WalkType.MORNING,  // default for Firestore
    var time: LocalTime = LocalTime.MIDNIGHT
)

enum class WalkType {
    MORNING, AFTERNOON, EVENING
}
