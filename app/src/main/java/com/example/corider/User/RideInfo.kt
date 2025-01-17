
package com.example.corider.User

import java.io.Serializable

data class RideInfo(
    var ride_id: String?= "",
    var user_id: String ="",
    var start_latitude: Double = 0.0,
    var start_longitude: Double = 0.0,
    var end_latitude: Double = 0.0,
    var end_longitude: Double = 0.0,
    var departure_date: String = "",
    var departure_time: String = "",
    var available_seats: Int = 0,
    var price_per_seat: Double = 0.0,
    var ride_status: String = "",
    var start_location: String = "",
    var end_location: String = "",
    var car_model: String = ""
) :  Serializable




