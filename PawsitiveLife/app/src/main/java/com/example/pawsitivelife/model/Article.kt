package com.example.pawsitivelife.model

data class Article (
    val title: String,
    val content: String,
    var tags: List<String>,
    var ageCategory: List<String> = listOf()
    )