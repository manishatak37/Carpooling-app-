package com.example.corider.model

data class RideFeedback(
    val rideId: Int,
    val driverId: Int,
    val riderId: Int,
    val rating: Float,
    val feedback: String
)

