package com.example.corider.model

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.R


import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

public class SearchRideForm : AppCompatActivity() {
    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var passengerInput: EditText
    private lateinit var contactInput: EditText
    private lateinit var submitButton: Button

    private lateinit var database: DatabaseReference

    private var pickupLatitude: Double? = null
    private var pickupLongitude: Double? = null
    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_rideform)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Initialize views
        dateInput = findViewById(R.id.date_ride)
        timeInput = findViewById(R.id.time_ride)
        passengerInput = findViewById(R.id.rider_input)
        contactInput = findViewById(R.id.contact_input)
        submitButton = findViewById(R.id.submit_button)

        // Retrieve pickup and destination coordinates from SharedPreferences
        loadLocationPreferences()

        // Set up click listeners for date and time pickers
        dateInput.setOnClickListener { showDatePicker() }
        timeInput.setOnClickListener { showTimePicker() }

        // Set up click listeners for submit button
        submitButton.setOnClickListener { submitSearch()

        }
    }

    private fun loadLocationPreferences() {
        val sharedPreferences = getSharedPreferences("SearchRidePrefs", MODE_PRIVATE)

        // Load pickup location
        pickupLatitude = sharedPreferences.getFloat("pickup_latitude", 0f).toDouble()
        pickupLongitude = sharedPreferences.getFloat("pickup_longitude", 0f).toDouble()

        // Load destination location
        val destinationPrefs = getSharedPreferences("DestinationPrefs", MODE_PRIVATE)
        destinationLatitude = destinationPrefs.getFloat("destination_latitude", 0f).toDouble()
        destinationLongitude = destinationPrefs.getFloat("destination_longitude", 0f).toDouble()

        Log.d("SearchRideForm", "Loaded pickup location: Latitude = $pickupLatitude, Longitude = $pickupLongitude")
        Log.d("SearchRideForm", "Loaded destination location: Latitude = $destinationLatitude, Longitude = $destinationLongitude")
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dateInput.setText(formattedDate)
            }, year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this, { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                timeInput.setText(formattedTime)
            }, hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun submitSearch() {
        // Get input values
        val date = dateInput.text.toString().trim()
        val time = timeInput.text.toString().trim()
        val passengersInput = passengerInput.text.toString().trim()
        val passengers = passengersInput.toInt()
        val contact = contactInput.text.toString().trim()

        // Validate inputs
        if (date.isEmpty() || time.isEmpty() || passengersInput.isEmpty() || contact.isEmpty()) {
            Log.e("SearchRideForm", "Please fill in all fields.")
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save the search ride details to Firebase
        val rideSearchId = database.push().key ?: return
        val rideSearchData = RideSearch(
            date = date,
            time = time,
            passengers = passengers,
            contact = contact,
            pickupLatitude = pickupLatitude ?: 0.0,
            pickupLongitude = pickupLongitude ?: 0.0,
            destinationLatitude = destinationLatitude ?: 0.0,
            destinationLongitude = destinationLongitude ?: 0.0
        )

        // Store the data under a "ride_search" node in Firebase Realtime Database
        database.child("ride_search").child(rideSearchId).setValue(rideSearchData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SearchRideForm", "Ride search submitted successfully.")
                    Toast.makeText(this, "Ride search submitted.", Toast.LENGTH_SHORT).show()

                    // Move to the next activity or page
                    //val intent = Intent(this, ride_resultsonsearch::class.java)
                    val intent = Intent(this, ride_resultsonsearch::class.java)
                    intent.putExtra("pickup_latitude", pickupLatitude)
                    intent.putExtra("pickup_longitude", pickupLongitude)
                    intent.putExtra("destination_latitude", destinationLatitude)
                    intent.putExtra("destination_longitude", destinationLongitude)
                    startActivity(intent)

                    // Clear the input fields
                    clearInputs()
                } else {
                    Log.e("SearchRideForm", "Failed to submit ride search.", task.exception)
                    Toast.makeText(this, "Failed to submit ride search.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun clearInputs() {
        dateInput.text.clear()
        timeInput.text.clear()
        passengerInput.text.clear()
        contactInput.text.clear()
    }

    data class RideSearch(
        val date: String,
        val time: String,
        val passengers: Int,
        val contact: String,
        val pickupLatitude: Double,
        val pickupLongitude: Double,
        val destinationLatitude: Double,
        val destinationLongitude: Double
    )
}
