package com.example.pawsitivelife.model

import java.time.LocalTime

enum class FeedType {
    MORNING, AFTERNOON, EVENING
}

data class FixedFeedTime(
    val type: FeedType,
    val time: LocalTime
)
