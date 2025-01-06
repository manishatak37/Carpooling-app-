package com.example.corider.model

data class UserProfile(
    val userId :String = "",
    val name: String = "",
    val about: String = "",
    val vehicleModel: String = "",
    val vehicleNumber: String = "",
    val seats: Int = 0,
    val travelPreferences: String? = null
)