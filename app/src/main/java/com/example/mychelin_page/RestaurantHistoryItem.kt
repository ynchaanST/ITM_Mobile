package com.example.mychelin_page

data class RestaurantHistoryItem(
    val name: String,
    val address: String,
    val rating: Int,
    val lastVisited: String,
    val totalSpent: Double
)