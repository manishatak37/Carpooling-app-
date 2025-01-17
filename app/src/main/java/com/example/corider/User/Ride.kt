package com.example.corider.User

data class Ride(
    val driverName: String, // You can change this if needed based on your data structure
    val pickupLocation: String,
    val dropoffLocation: String,
    val carModel: String,
    val departureDate: String,
    val departureTime: String,
    val pricePerSeat: Double
)