
package com.example.corider.User

fun RideInfo.toRide(): Ride {
    return Ride(
        driverName = "xyz", // Assuming 'user_id' represents the driver's name or some identifier
        pickupLocation = "Lat: ${this.start_latitude}, Long: ${this.start_longitude}",
        dropoffLocation = "Lat: ${this.end_latitude}, Long: ${this.end_longitude}",
        carModel = this.car_model,
        departureDate = this.departure_date,
        departureTime = this.departure_time,
        pricePerSeat = this.price_per_seat
    )
}
