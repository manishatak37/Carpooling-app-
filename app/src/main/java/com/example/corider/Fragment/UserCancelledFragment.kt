package com.example.corider.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.R
import com.example.corider.User.UserDisplayRideAdapter
import com.google.firebase.database.*



class UserCancelledFragment : Fragment(R.layout.activity_user_cancelled_fragment) {

    private lateinit var database: DatabaseReference
    private val rideInfoList = ArrayList<RideInfo>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserDisplayRideAdapter

    private var userId: String = ""  // Replace with the actual user ID logic

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)

        initializeDatabase()
        userId = getUserIdFromPreferences()
        fetchCompletedRides(userId)
    }

    // Setup RecyclerView with layout manager and adapter
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        adapter = UserDisplayRideAdapter(rideInfoList)
        recyclerView.adapter = adapter
    }
    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences?.getString("userID", "") ?: ""  // Safely handle null case
    }
    // Initialize Firebase Database reference
    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    // Fetch completed/cancelled rides using user ID, then fetch ride details
    private fun fetchCompletedRides(userId: String) {
        // Query the Booking table to get ride IDs associated with the user
        val bookingRef = database.child("bookings").orderByChild("userId").equalTo(userId)

        bookingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rideIds = mutableListOf<String>()
                for (data in snapshot.children) {
                    val booking = data.getValue(Booking::class.java)
                    booking?.rideId?.let { rideIds.add(it) } // Collect ride IDs
                }

                // Print the rideIds for debugging
                Log.d("UserCompletedFragment", "Fetched Ride IDs: $rideIds")

                if (rideIds.isNotEmpty()) {
                    // After fetching ride IDs, query the RideInfo table to get the ride details
                    fetchRideDetails(rideIds)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace() // Handle the error
            }
        })
    }


    // Fetch ride details from RideInfo table based on the ride IDs
    private fun fetchRideDetails(rideIds: List<String>) {
        val rideInfoRef = database.child("RideInfo")
        rideInfoList.clear() // Clear the list before adding new data

        // Set up a single listener to fetch data for all rides
        rideIds.forEach { rideId ->
            rideInfoRef.child(rideId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ride = snapshot.getValue(RideInfo::class.java)
                    if (ride != null && ride.ride_status == "cancelled") {
                        ride.category = ""// Check if the ride is completed
                        rideInfoList.add(ride) // Add ride details to the list
                    }
                    adapter.notifyDataSetChanged() // Refresh UI after adding data
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace() // Handle the error
                }
            })
        }
    }
}
