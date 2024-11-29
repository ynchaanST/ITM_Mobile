package com.example.mychelin_page

data class NaverSearchResponse(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<NaverPlaceItem>
)



