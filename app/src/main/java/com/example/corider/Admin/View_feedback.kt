package com.example.corider.Admin

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.R
import com.example.corider.model.RideFeedback
import com.google.firebase.database.*

class View_feedback : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_feedback)

        val tableLayout = findViewById<TableLayout>(R.id.tableLayoutFeedback)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("driver_feedback")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tableLayout.removeAllViews()

                val feedbackList = mutableListOf<RideFeedback>()

                // Extract and map snapshots to RideFeedback objects
                for (feedbackSnapshot in snapshot.children) {
                    val rideFeedback = feedbackSnapshot.toRideFeedback()
                    if (rideFeedback != null) {
                        feedbackList.add(rideFeedback)
                    }
                }

                // Sort the list by driver rating in descending order
                val sortedFeedbackList = feedbackList.sortedByDescending { it.rating }

                // Create header row
                val headerRow = TableRow(this@View_feedback).apply {
                    setBackgroundColor(resources.getColor(R.color.teal_700))
                    setPadding(8, 8, 8, 8)
                }

                val headers = listOf("Ride ID", "Driver ID", "User ID", "Rating", "Feedback")
                headers.forEach { headerText ->
                    val headerView = TextView(this@View_feedback).apply {
                        text = headerText
                        setTextColor(resources.getColor(android.R.color.white))
                        setTypeface(null, Typeface.BOLD)
                        setPadding(16, 16, 16, 16)
                    }
                    headerRow.addView(headerView)
                }

                tableLayout.addView(headerRow)

                // Populate the table with sorted feedback
                for (rideFeedback in sortedFeedbackList) {
                    val tableRow = TableRow(this@View_feedback).apply {
                        setPadding(8, 8, 8, 8)
                    }

                    val rideIdView = TextView(this@View_feedback).apply {
                        text = rideFeedback.rideId.toString()
                        setPadding(8, 8, 8, 8)
                    }
                    tableRow.addView(rideIdView)

                    val driverIdView = TextView(this@View_feedback).apply {
                        text = rideFeedback.driverId.toString()
                        setPadding(8, 8, 8, 8)
                    }
                    tableRow.addView(driverIdView)

                    val userIdView = TextView(this@View_feedback).apply {
                        text = rideFeedback.riderId.toString()
                        setPadding(8, 8, 8, 8)
                    }
                    tableRow.addView(userIdView)

                    val ratingView = TextView(this@View_feedback).apply {
                        text = rideFeedback.rating.toString()
                        setPadding(8, 8, 8, 8)
                    }
                    tableRow.addView(ratingView)

                    val feedbackView = TextView(this@View_feedback).apply {
                        text = rideFeedback.feedback
                        setPadding(8, 8, 8, 8)
                    }
                    tableRow.addView(feedbackView)

                    tableLayout.addView(tableRow)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("View_feedback", "Database error: ${error.message}")
            }
        })

    }

    private fun deleteFeedback(key: String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("ride_feedback")

        if (key.isNotEmpty()) {
            myRef.child(key).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("View_feedback", "Feedback deleted successfully")
                } else {
                    Log.e("View_feedback", "Failed to delete feedback: ${task.exception?.message}")
                }
            }
        }
    }

    // Extension function to manually map snapshot to RideFeedback
    private fun DataSnapshot.toRideFeedback(): RideFeedback? {
        return try {
            val rideId = (child("rideId").value as? Number)?.toInt() ?: 0
            val driverId = (child("driverId").value as? Number)?.toInt() ?: 0
            val userId = (child("riderId").value as? Number)?.toInt() ?: 0
            val rating = (child("rating").value as? Number)?.toFloat() ?: 0.0f
            val feedback = child("feedback").value as? String ?: ""

            RideFeedback(rideId, driverId, userId, rating, feedback)
        } catch (e: Exception) {
            Log.e("View_feedback", "Error mapping snapshot: ${e.message}")
            null
        }
    }

}
