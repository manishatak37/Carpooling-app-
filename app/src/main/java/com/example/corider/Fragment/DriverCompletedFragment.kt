package com.example.corider.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.Driver.DriverDisplayRideAdapter
import com.example.corider.R
import com.google.firebase.database.*


data class RideInfo(
    val driver_id: String = "",
    val ride_id:String = "",
    val start_location: String = "",
    val end_location: String = "",
    val departure_date: String = "",
    val departure_time: String = "",
    val price_per_seat: Int = 0,
    val ride_status: String = "",
    var category: String = "" // Changed from val to var
)


class DriverCompletedFragment : Fragment(R.layout.activity_driver_completed_fragment) {

    private lateinit var database: DatabaseReference
    private val rideInfoList = ArrayList<RideInfo>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DriverDisplayRideAdapter
    private var userId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)

        initializeDatabase()
        userId = getUserIdFromPreferences()
        Log.d("UserID", "$userId")
        fetchCancelledRides()
    }

    // Setup RecyclerView with layout manager and adapter
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        adapter = DriverDisplayRideAdapter(rideInfoList)
        recyclerView.adapter = adapter
    }

    // Initialize Firebase Database reference
    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().getReference("RideInfo")
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences?.getString("userID", "") ?: ""  // Safely handle null case
    }

    // Fetch cancelled rides from Firebase and update RecyclerView
    // Fetch rides with "scheduled" status and driver_id = "1" from Firebase and update RecyclerView
    private fun fetchCancelledRides() {
        database.orderByChild("driver_id").equalTo(userId) // Filter by driver_id = "1"
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rideInfoList.clear() // Avoid duplicates
                    for (data in snapshot.children) {
                        val ride = data.getValue(RideInfo::class.java)
                        if (ride != null && ride.ride_status == "completed") {
                            // Add to the list if ride_status is "scheduled"
                            ride.category = ""
                            rideInfoList.add(ride)
                        }
                    }
                    adapter.notifyDataSetChanged() // Refresh UI
                }

                override fun onCancelled(error: DatabaseError) {
                    // Log or handle database error
                    error.toException().printStackTrace()
                }
            })
    }

}
