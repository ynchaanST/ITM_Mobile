package com.example.mychelin_page

data class NotificationData(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val type: String = "RESERVATION", // RESERVATION, SYSTEM 등
    val reservationId: String? = null
)