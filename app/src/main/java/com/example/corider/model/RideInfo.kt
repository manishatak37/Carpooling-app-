package com.example.corider.model

data class RideInfo(
    val ride_id: String,
    val user_id: String,
    val start_latitude: Double,
    val start_longitude: Double,
    val end_latitude: Double,
    val end_longitude: Double,
    val departure_date: String,
    val departure_time: String,
    val available_seats: Int,
    val price_per_seat: Double,
    val ride_status: String,
    val car_model: String
)



