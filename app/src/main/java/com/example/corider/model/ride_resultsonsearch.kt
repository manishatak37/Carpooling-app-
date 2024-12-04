package com.example.corider

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.adapter.RideAdapter
import com.example.corider.model.Ride
import com.example.corider.model.RideInfo
import com.example.corider.model.toRide
import com.google.firebase.database.*

public class ride_resultsonsearch : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RideAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serch_ride_resultpage)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("RideInfo")

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.searchResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RideAdapter(listOf())
        recyclerView.adapter = adapter

        // Fetch search criteria from intent or SharedPreferences
        val searchLatitude = intent.getDoubleExtra("pickup_latitude", 0.0)
        val searchLongitude = intent.getDoubleExtra("pickup_longitude", 0.0)
        val searchDate = intent.getStringExtra("departure_date")
        val searchTime = intent.getStringExtra("departure_time")

        // Search for rides in Firebase
        fetchRides(searchLatitude, searchLongitude, searchDate, searchTime)
    }

    private fun fetchRides(
        searchLatitude: Double,
        searchLongitude: Double,
        searchDate: String?,
        searchTime: String?
    ) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ridesList = mutableListOf<Ride>()
                for (rideSnapshot in snapshot.children) {
                    var rideInfo = rideSnapshot.getValue(RideInfo::class.java)
                    if (rideInfo != null) {
                        // Handle conversion for each field that might be a Long or String
                        rideInfo.departure_date = rideSnapshot.child("departure_date").value?.let {
                            when (it) {
                                is Long -> it.toString()  // Convert Long to String if it's stored as Long
                                is String -> it            // Keep it as String if already a String
                                else -> ""                 // Default to empty string for any other type
                            }
                        } ?: ""  // Default to empty string if departure_date is null

                        rideInfo.ride_id = rideSnapshot.child("ride_id").value?.let {
                            when (it) {
                                is Long -> it.toString()  // Convert Long to String if it's stored as Long
                                is String -> it            // Keep it as String if already a String
                                else -> ""                 // Default to empty string for other types
                            }
                        } ?: ""  // Default to empty string if ride_id is null

                        rideInfo.start_latitude = rideSnapshot.child("start_latitude").value?.let {
                            when (it) {
                                is Double -> it             // Keep it as Double if already a Double
                                is Long -> it.toDouble()    // Convert Long to Double if it's stored as Long
                                is String -> it.toDoubleOrNull() ?: 0.0 // Convert String to Double safely
                                else -> 0.0                 // Default to 0.0 for any other type
                            }
                        } ?: 0.0  // Default to 0.0 if start_latitude is null

                        rideInfo.start_longitude = rideSnapshot.child("start_longitude").value?.let {
                            when (it) {
                                is Double -> it             // Keep it as Double if already a Double
                                is Long -> it.toDouble()    // Convert Long to Double if it's stored as Long
                                is String -> it.toDoubleOrNull() ?: 0.0 // Convert String to Double safely
                                else -> 0.0                 // Default to 0.0 for any other type
                            }
                        } ?: 0.0  // Default to 0.0 if start_longitude is null

                        rideInfo.end_latitude = rideSnapshot.child("end_latitude").value?.let {
                            when (it) {
                                is Double -> it
                                is Long -> it.toDouble()
                                is String -> it.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                        } ?: 0.0

                        rideInfo.end_longitude = rideSnapshot.child("end_longitude").value?.let {
                            when (it) {
                                is Double -> it
                                is Long -> it.toDouble()
                                is String -> it.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                        } ?: 0.0

                        // Add more fields if necessary in a similar manner

                        if (isExactMatch(rideInfo, searchLatitude, searchLongitude, searchDate, searchTime)) {
                            ridesList.add(rideInfo.toRide())  // Convert RideInfo to Ride
                        } else if (isNearbyMatch(rideInfo, searchLatitude, searchLongitude)) {
                            ridesList.add(rideInfo.toRide())  // Convert RideInfo to Ride
                        }
                    }
                }
                updateRecyclerView(ridesList)
            }


            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchResults", "Database error: ${error.message}")
                Toast.makeText(this@ride_resultsonsearch, "Failed to fetch rides.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isExactMatch(
        ride: RideInfo,
        searchLatitude: Double,
        searchLongitude: Double,
        searchDate: String?,
        searchTime: String?
    ): Boolean {
        return ride.start_latitude == searchLatitude &&
                ride.start_longitude == searchLongitude &&
                ride.departure_date == searchDate &&
                ride.departure_time == searchTime
    }

    private fun isNearbyMatch(ride: RideInfo, searchLatitude: Double, searchLongitude: Double): Boolean {
        val distance = calculateDistance(
            searchLatitude, searchLongitude,
            ride.start_latitude, ride.start_longitude
        )
        return distance <= 2.0 // Nearby threshold in kilometers
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Radius of the Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun updateRecyclerView(ridesList: List<Ride>) {
        if (ridesList.isEmpty()) {
            Toast.makeText(this, "No rides found.", Toast.LENGTH_SHORT).show()
        }
        adapter.updateData(ridesList)
    }
}
