package com.example.corider.User

data class aBookingDetails(
    val rideId: Int = 0,         // Default values for Firebase compatibility
    val userId: String = "",
    val seatsToBook: Int = 0,
    val bookingTime: Long = 0L
)