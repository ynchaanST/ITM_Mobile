package com.example.mychelin_page

data class NaverSearchResponse(
    val items: List<NaverPlaceItem>
)

data class NaverPlaceItem(
    val title: String,
    val category: String,
    val description: String,
    val telephone: String,
    val address: String,
    val roadAddress: String,
    val mapx: String,
    val mapy: String,
    val link: String
)
