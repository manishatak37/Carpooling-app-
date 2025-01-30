package com.example.corider.Driver

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.R
import com.example.corider.adapter.NotificationAdapter
import com.example.corider.model.Notification
import com.google.firebase.database.*

class DriverInbox : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationList: MutableList<Notification>
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_inbox)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView and notification list
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notificationList = mutableListOf()
        notificationAdapter = NotificationAdapter(this, notificationList)
        recyclerView.adapter = notificationAdapter

        // Query Firebase to get notifications for the specific userId
        fetchNotifications()
    }


    private fun fetchNotifications() {
        // Get the userId using the utility function
        val userId = getUserIdFromPreferences()

        if (userId != null) {
            // Query Firebase to get notifications for the current user
            database.child("notifications")
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        notificationList.clear()  // Clear previous data

                        // Loop through the snapshot and get notifications
                        for (dataSnapshot in snapshot.children) {
                            val notification = dataSnapshot.getValue(Notification::class.java)
                            notification?.let {
                                notificationList.add(it)
                            }
                        }

                        // Notify the adapter about data changes
                        notificationAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                        Toast.makeText(this@DriverInbox, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            // Handle case where userId is not available
            Toast.makeText(this@DriverInbox, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("userID", null)
    }

}
