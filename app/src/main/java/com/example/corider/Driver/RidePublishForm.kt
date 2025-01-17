package com.example.corider.Driver

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
import java.text.SimpleDateFormat
import java.util.*

class RidePublishForm : AppCompatActivity() {

    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var passengerInput: EditText
    private lateinit var carModelInput: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_publish_form) // Ensure this matches your layout file name

        // Initialize views
        dateInput = findViewById(R.id.date_input)
        timeInput = findViewById(R.id.time_input)
        passengerInput = findViewById(R.id.passenger_input)
        carModelInput = findViewById(R.id.car_model_input)
        submitButton = findViewById(R.id.submit_button)

        // Set up click listeners
        dateInput.setOnClickListener { showDatePicker() }
        timeInput.setOnClickListener { showTimePicker() }
        submitButton.setOnClickListener { publishRide() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            dateInput.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            timeInput.setText(String.format("%02d:%02d", hourOfDay, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePickerDialog.show()
    }

    private fun publishRide() {
        // Get input values
        val date = dateInput.text.toString().trim()
        val time = timeInput.text.toString().trim()
        val passengersInput = passengerInput.text.toString().trim()
        val carModel = carModelInput.text.toString().trim()

        if (date.isEmpty() || time.isEmpty() || passengersInput.isEmpty() || carModel.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = sdf.parse(date)
        val currentDate = Calendar.getInstance().time
        val maxDate = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time

        if (selectedDate.before(currentDate)) {
            Toast.makeText(this, "Date cannot be in the past.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDate.after(maxDate)) {
            Toast.makeText(this, "Date cannot be more than one month from now.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate time if the selected date is today
        if (sdf.format(selectedDate) == sdf.format(currentDate)) {
            val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val selectedTime = timeSdf.parse(time)
            val currentTime = Calendar.getInstance().time
            if (selectedTime.before(currentTime)) {
                Toast.makeText(this, "Time cannot be in the past for today.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Validate passengers
        val passengers = passengersInput.toIntOrNull()
        if (passengers == null || passengers < 1 || passengers > 4) {
            Toast.makeText(this, "Passengers must be between 1 and 4.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate car model (example: alphanumeric check)
        if (!carModel.matches(Regex("^[A-Za-z0-9\\s-]{1,20}$"))) {
            Toast.makeText(this, "Invalid car model. Only alphanumeric and dashes allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save the ride details in SharedPreferences
        val sharedPreferences = getSharedPreferences("RideDetails", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("departure_date", date)
            putString("departure_time", time)
            putInt("available_seats", passengers)
            putString("car_model", carModel)
            apply()
        }

        // Log the ride details
        Log.d("RidePublishForm", "Ride Published: $date at $time, Passengers: $passengers, Car Model: $carModel")

        // Start RidePublishForm2 activity
        val intent = Intent(this, RidePublishForm2::class.java)
        startActivity(intent)

        // Clear the inputs
        clearInputs()
    }

    private fun clearInputs() {
        dateInput.text.clear()
        timeInput.text.clear()
        passengerInput.text.clear()
        carModelInput.text.clear()
    }
}
