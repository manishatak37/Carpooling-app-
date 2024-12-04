/*package com.example.corider.model

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
)*/

package com.example.corider.model

data class RideInfo(
    var ride_id: String = "",
    var user_id: String = "",
    var start_latitude: Double = 0.0,
    var start_longitude: Double = 0.0,
    var end_latitude: Double = 0.0,
    var end_longitude: Double = 0.0,
    var departure_date: String = "",
    var departure_time: String = "",
    var available_seats: Int = 0,
    var price_per_seat: Double = 0.0,
    var ride_status: String = "",
    var car_model: String = ""
)



