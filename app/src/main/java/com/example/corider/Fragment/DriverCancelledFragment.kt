package com.example.corider.Fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.Driver.DriverDisplayRideAdapter
import com.example.corider.R
import com.google.firebase.database.*



class DriverCancelledFragment : Fragment(R.layout.activity_driver_cancelled_fragment) {

    private lateinit var database: DatabaseReference
    private val rideInfoList = ArrayList<RideInfo>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DriverDisplayRideAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)

        initializeDatabase()
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

    // Fetch cancelled rides from Firebase and update RecyclerView
    // Fetch rides with "scheduled" status and driver_id = "1" from Firebase and update RecyclerView
    private fun fetchCancelledRides() {
        database.orderByChild("driver_id").equalTo("zntFzJmA1QTeaEc6EBVcSpRUhed2") // Filter by driver_id = "1"
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rideInfoList.clear() // Avoid duplicates
                    for (data in snapshot.children) {
                        val ride = data.getValue(RideInfo::class.java)
                        if (ride != null && ride.ride_status == "cancelled") {
                            // Add to the list if ride_status is "scheduled"
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
