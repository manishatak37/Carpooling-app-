/*package com.example.corider.model

data class RideInfo(
    val ride_id: Int,
    val user_id: Int,
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

/*package com.example.corider.model

data class RideInfo(
    var ride_id: Int,
    var user_id: Int,
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

 */
package com.example.corider.User

import java.io.Serializable

data class RideInfo(
    var ride_id: String?= "",
    var driver_id: String ="",
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



