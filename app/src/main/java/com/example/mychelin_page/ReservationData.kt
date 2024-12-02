package com.example.mychelin_page

data class ReservationData(
    var reservationId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val userId: String = "",
    val tableNumber: Int = 0,
    val reservationDate: Long = 0,
    val numberOfGuests: Int = 0,
    val specialRequests: String = "",
    val status: String = "PENDING" // PENDING, CONFIRMED, CANCELLED
)