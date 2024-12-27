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
        // Query Firebase to get notifications for userId "1" (adjust as per actual user ID)
        database.child("notifications")
            .orderByChild("userId")
            .equalTo("1") // Adjust as per the user ID
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
    }
}
