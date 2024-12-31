package com.example.corider.model

data class RideInfoFetch(
    val start_location: String = "",
    val end_location: String = "",
    val departure_date: String = "",
    val departure_time: String = "",
    val price_per_seat: Int = 0,
    val ride_status: String = ""
)
