package com.example.corider.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.R
import com.example.corider.adapter.NotificationAdapter
import com.example.corider.model.Notification
import com.google.firebase.database.*

class InboxFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationList: MutableList<Notification>
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        // Initialize RecyclerView and notification list
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationList = mutableListOf()
        notificationAdapter = NotificationAdapter(requireContext(), notificationList)
        recyclerView.adapter = notificationAdapter

        // Query Firebase to get notifications where userId is "1"
        fetchNotifications()

        return view
    }

    private fun fetchNotifications() {
        // Query Firebase to get notifications for userId "1"
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
                }
            })
    }
}
