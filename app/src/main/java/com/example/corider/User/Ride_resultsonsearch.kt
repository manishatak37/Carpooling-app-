package com.example.corider.User

import com.example.corider.User.RideAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import android.location.Geocoder
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.corider.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*


class ride_resultsonsearch : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RideAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serch_ride_resultpage)

        auth = Firebase.auth
                // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("RideInfo")

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.searchResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RideAdapter(listOf(),this)
        recyclerView.adapter = adapter

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("BookRideDetails", Context.MODE_PRIVATE)

        // Fetch search criteria from intent
        val pickupLatitude = intent.getDoubleExtra("pickup_latitude", 0.0)
        val pickupLongitude = intent.getDoubleExtra("pickup_longitude", 0.0)
        val destinationLatitude = intent.getDoubleExtra("destination_latitude", 0.0)
        val destinationLongitude = intent.getDoubleExtra("destination_longitude", 0.0)

        // Log the values to ensure they are correct
        Log.d("SearchResults", "Pickup: $pickupLatitude, $pickupLongitude")
        Log.d("SearchResults", "Destination: $destinationLatitude, $destinationLongitude")

        // Search for rides in Firebase
        fetchRides(pickupLatitude, pickupLongitude, destinationLatitude, destinationLongitude)
    }


    private fun fetchRides(
        pickupLatitude: Double,
        pickupLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ridesList = mutableListOf<RideInfo>()
                for (rideSnapshot in snapshot.children) {
                    val rideInfo = rideSnapshot.getValue(RideInfo::class.java)
                    if (rideInfo != null) {
                        if (rideInfo.available_seats > 0) {

                            // Fetch human-readable place names
                            val pickupPlace = getPlaceName(rideInfo.start_latitude, rideInfo.start_longitude)
                            val destinationPlace = getPlaceName(rideInfo.end_latitude, rideInfo.end_longitude)

                            // Log each ride data
                            Log.d("SearchResults", "Pickup: $pickupPlace, Destination: $destinationPlace")

                            // Range-based comparison
                            if (isWithinRange(rideInfo, pickupLatitude, pickupLongitude, destinationLatitude, destinationLongitude)) {
                                // Update RideInfo to include human-readable names
                                rideInfo.start_location = pickupPlace
                                rideInfo.end_location = destinationPlace
                                ridesList.add(rideInfo)
                            }
                        }
                    }}
                Log.d("Fetched Rides : ", ridesList.toString())
                updateRecyclerView(ridesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchResults", "Database error: ${error.message}")
                Toast.makeText(this@ride_resultsonsearch, "Failed to fetch rides.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun storeRideDetails(pricePerSeat: Double, seatsToBook: Int) {
        // Calculate the total price
        val totalPrice = pricePerSeat * seatsToBook

        // Store the details in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putInt("seatsBooked", seatsToBook)
        editor.putFloat("totalPrice", totalPrice.toFloat())
        editor.apply()

        // Log for debugging
        Log.d("RideDetails", " Seats: $seatsToBook, Total Price: $totalPrice")
    }

    private fun getPlaceName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0) // Get full address as a readable place name
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Error fetching address: ${e.message}")
            "Unknown Location"
        }
    }

    // Function to calculate distance between two latitude/longitude points using the Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius of Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c // Distance in km
    }

    // Function to check if two points are within range (in km)
    private fun isWithinRange(
        ride: RideInfo,
        pickupLatitude: Double,
        pickupLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        maxDistanceKm: Double = 5.0 // Max distance in km
    ): Boolean {
        val pickupDistance = calculateDistance(ride.start_latitude, ride.start_longitude, pickupLatitude, pickupLongitude)
        val destinationDistance = calculateDistance(ride.end_latitude, ride.end_longitude, destinationLatitude, destinationLongitude)

        // Check if both pickup and destination distances are within the acceptable range
        return pickupDistance <= maxDistanceKm && destinationDistance <= maxDistanceKm
    }

    private fun updateRecyclerView(ridesList: List<RideInfo>) {
        adapter.updateData(ridesList)

        adapter.setOnItemClickListener { rideInfo, seatsToBook ->
            // Assuming `RideAdapter` calls this function on ride selection
            if (seatsToBook > 0) {
                storeRideDetails(rideInfo.price_per_seat, seatsToBook)
                Toast.makeText(this, "Ride booked successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please select at least one seat.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}