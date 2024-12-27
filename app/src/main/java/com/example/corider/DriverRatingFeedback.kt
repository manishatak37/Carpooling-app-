package com.example.corider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class DriverRatingFeedback : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var feedbackEditText: EditText
    private lateinit var submitButton: Button
    private var rideId: Int = 0
    private var driverId: Int = 0
    private var riderId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_rating_feedback)

        ratingBar = findViewById(R.id.ratingBar)
        feedbackEditText = findViewById(R.id.feedbackEditText)
        submitButton = findViewById(R.id.submitButton)

        // Initialize rideId, driverId, and riderId
        initializeIds()

        submitButton.setOnClickListener {
            submitFeedback()
        }
    }

    // Initialize IDs for demonstration
    private fun initializeIds() {
        rideId = 1
        driverId = 1
        riderId = 1
    }

    private fun submitFeedback() {
        val rating = ratingBar.rating
        val feedback = feedbackEditText.text.toString()

        if (rating > 0 && feedback.isNotEmpty()) {
            saveFeedbackToDatabase(rating, feedback)
        } else {
            Toast.makeText(this, "Please provide a rating and feedback", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFeedbackToDatabase(rating: Float, feedback: String) {
        // Reference to the 'feedback' table
        val feedbackRef = FirebaseDatabase.getInstance().getReference("driver_feedback")
        val feedbackData = hashMapOf<String, Any>(
            "rideId" to rideId,
            "driverId" to driverId,
            "riderId" to riderId,
            "rating" to rating,
            "feedback" to feedback
        )

        feedbackRef.push().setValue(feedbackData)
            .addOnSuccessListener {
                Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                //updateDriverRating(rating)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDriverRating(newRating: Float) {
        val driverRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")

        driverRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentAverage = snapshot.child("averageRating").getValue<Double>() ?: 0.0
                val totalRatings = snapshot.child("totalRatings").getValue<Int>() ?: 0

                // Calculate new average rating
                val updatedAverage = (currentAverage * totalRatings + newRating) / (totalRatings + 1)

                // Update driver record in database
                val driverUpdates = hashMapOf<String, Any>(
                    "averageRating" to updatedAverage,
                    "totalRatings" to totalRatings + 1
                )

                driverRef.updateChildren(driverUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(this@DriverRatingFeedback, "Driver's rating updated", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after feedback submission
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@DriverRatingFeedback, "Failed to update driver's rating", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DriverRatingFeedback, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
