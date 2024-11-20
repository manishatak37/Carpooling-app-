package com.example.corider

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.corider.model.RideInfo
import com.example.corider.model.TransactionInfo

class Admin_home_page : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userDatabase: DatabaseReference
    private lateinit var transactionDatabase: DatabaseReference
    private lateinit var rideDatabase: DatabaseReference
    private lateinit var userCountTextView: TextView
    private lateinit var completedRidesCountTextView: TextView
    private lateinit var sumOfTransactionTextView: TextView // Fixed variable naming

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home_page)

        // Initialize FirebaseAuth and FirebaseDatabase reference
        auth = FirebaseAuth.getInstance()
        userDatabase = FirebaseDatabase.getInstance().getReference("users")
        rideDatabase = FirebaseDatabase.getInstance().getReference("ride_info")
        transactionDatabase = FirebaseDatabase.getInstance().getReference("transactions") // Fixed reference to transactions

        // Get reference to TextViews
        userCountTextView = findViewById(R.id.textView7)
        completedRidesCountTextView = findViewById(R.id.textView10)
        sumOfTransactionTextView = findViewById(R.id.textView13)

        // Fetch number of registered users from the 'user' table
        getUserCount()
        // Fetch count of completed rides from the 'ride_info' table
        getCompletedRidesCount()
        // Fetch total sum of completed transactions
        getTotalCompletedTransactions()

//        onclick functions

        val viewRegisteredUsersTextView: TextView = findViewById(R.id.textView15) // Adjust the ID as needed

        // Set OnClickListener for the TextView
        viewRegisteredUsersTextView.setOnClickListener {
            val intent = Intent(this, RegisteredUsersActivity::class.java) // Create an Intent to start the new activity
            startActivity(intent) // Start the activity
        }
    }

    private fun getUserCount() {
        // Attach a listener to the 'user' table to get the count of users
        userDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Count the number of children (users) under 'user' node
                val userCount = snapshot.childrenCount
                // Update the TextView with the user count
                userCountTextView.text = userCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                userCountTextView.text = "Error loading"
            }
        })
    }

    private fun getCompletedRidesCount() {
        // Attach a listener to the 'ride_info' table to get the count of completed rides
        rideDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var completedRidesCount = 0

                // Iterate through the rides and count the ones with status "completed"
                for (rideSnapshot in snapshot.children) {
                    val rideStatus = rideSnapshot.child("ride_status").getValue(String::class.java)
                    if (rideStatus == "completed") {
                        completedRidesCount++
                    }
                }

                // Update the TextView with the count of completed rides
                completedRidesCountTextView.text = completedRidesCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                completedRidesCountTextView.text = "Error loading"
            }
        })
    }

    private fun getTotalCompletedTransactions() {
        // Attach a listener to the 'transactions' table to get the total amount of completed transactions
        transactionDatabase.orderByChild("transaction_status").equalTo("completed")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalAmount = 0.0
                    for (transactionSnapshot in snapshot.children) {
                        val transaction = transactionSnapshot.getValue(TransactionInfo::class.java)
                        totalAmount += transaction?.amount ?: 0.0
                    }
                    // Update the TextView with the total amount, formatted as needed
                    sumOfTransactionTextView.text = String.format("%.2f", totalAmount) // Formatting for two decimal places
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                    sumOfTransactionTextView.text = "Error loading"
                }
            })
    }
}