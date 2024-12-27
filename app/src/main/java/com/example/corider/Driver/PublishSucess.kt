package com.example.corider.Driver // Replace with your actual package name


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.MainActivity
import com.example.corider.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.corider.model.RideInfo

class PublishSucess : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_sucess)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize Firebase Database
        database = Firebase.database.reference
        Log.e("PublishSuccess", "Till year it is getting executed")
        // Initialize the continue button
        val continueButton: Button = findViewById(R.id.continue_button)

        // Retrieve ride details from SharedPreferences
        val rideDetails = loadRideDetails()

        if (rideDetails != null) {
            Log.e("PublishSuccess", "Till year it is getting executed")
            // Save the new ride to Firebase
            saveRideToDatabase(rideDetails)
        } else {
            // Handle the case where ride details are not available
            Log.e("PublishSuccess", "Failed to load ride details")
            // You may want to show an error message to the user here
        }

        continueButton.setOnClickListener {
            // Redirect to the main activity or any other page
            val intent = Intent(this, DriverNavigation::class.java) // Replace with your main activity
            startActivity(intent)
            finish() // Close the success page
        }
    }

    private fun loadRideDetails(): RideInfo? {
        val sharedPreferences = getSharedPreferences("RideDetails", MODE_PRIVATE)


        // Retrieve the user_id from SharedPreferences
        val userId = 1 // Assuming this is a constant or retrieved differently
//        val userId = auth.currentUser?.uid.toString()

        // Get the rest of the ride details from SharedPreferences
        val startLatitude = sharedPreferences.getFloat("start_latitude", -1f).toDouble()
        val startLongitude = sharedPreferences.getFloat("start_longitude", -1f).toDouble()
        val endLatitude = sharedPreferences.getFloat("end_latitude", -1f).toDouble()
        val endLongitude = sharedPreferences.getFloat("end_longitude", -1f).toDouble()
        val departureDate = sharedPreferences.getString("departure_date", null) ?: return null
        val departureTime = sharedPreferences.getString("departure_time", null) ?: return null
        val availableSeats = sharedPreferences.getInt("available_seats", -1)
        if (availableSeats == -1) return null
        val pricePerSeat = sharedPreferences.getFloat("cost", -1f).toDouble()
        val rideStatus = sharedPreferences.getString("ride_status", "scheduled") ?: "scheduled"
        val carModel = sharedPreferences.getString("car_model", null) ?: return null

        // Retrieve the current ride_counter from SharedPreferences
        val rideCounter = sharedPreferences.getInt("ride_counter", 1)  // Default to 1 or any number you like

        // Increment the ride_counter to generate the new ride_id
        val newRideId = rideCounter + 1

        // Return the new RideInfo object with the generated ride_id
        return RideInfo(
            ride_id = newRideId, // Convert to string to match the RideInfo model
            user_id = userId,
            start_latitude = startLatitude,
            start_longitude = startLongitude,
            end_latitude = endLatitude,
            end_longitude = endLongitude,
            departure_date = departureDate,
            departure_time = departureTime,
            available_seats = availableSeats,
            price_per_seat = pricePerSeat,
            ride_status = rideStatus,
            car_model = carModel
        )
    }

    private fun saveRideToDatabase(ride: RideInfo) {
        // Generate a new key under the "RideInfo" node
        val newRideKey = database.child("RideInfo").push().key

        // Ensure the key is not null
        if (newRideKey != null) {
            // Set the ride data under the new key
            database.child("RideInfo").child(newRideKey).setValue(ride)
                .addOnSuccessListener {
                    // Successfully saved the ride
                    Log.d("Database", "Ride data saved successfully")
                }
                .addOnFailureListener {
                    // Handle failure to save ride data
                    Log.e("Database", "Failed to save ride data", it)
                }
        } else {
            Log.e("Database", "Failed to generate a new ride key")
        }
    }

}
