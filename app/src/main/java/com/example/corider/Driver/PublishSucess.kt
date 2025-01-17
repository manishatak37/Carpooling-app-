package com.example.corider // Replace with your actual package name


import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.Driver.DriverNavigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.location.Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.io.Serializable

data class RideInfo(
    var ride_id: String= "",
    var driver_id: String,
    var start_latitude: Double = 0.0,
    var start_longitude: Double = 0.0,
    var end_latitude: Double = 0.0,
    var end_longitude: Double = 0.0,
    var departure_date: String = "",
    var departure_time: String = "",
    var available_seats: Int = 0,
    var price_per_seat: Double = 0.0,
    var ride_status: String = "",
    var start_location: String = "",
    var end_location: String = "",
    var car_model: String = ""
) :  Serializable

class PublishSucess : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_sucess)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize Firebase Database
        database = Firebase.database.reference
        Log.e("PublishSuccess", "Till year it is getting executed")
        // Initialize the continue button
        val continueButton: Button = findViewById(R.id.continue_button)

        // Retrieve ride details from SharedPreferences
        val rideDetails = loadRideDetails()
//        if (!userEmail.isNullOrEmpty()) {
//            sendGmailNotification(userEmail, rideDetails)
//        }


        if (rideDetails != null) {
            Log.e("PublishSuccess", "Till year it is getting executed")
            // Save the new ride to Firebase
            saveRideToDatabase(rideDetails)
            if (rideDetails != null) {
                sendGmailNotification("jagrutisingh1519@gmail.com", rideDetails)
            }
        } else {
            // Handle the case where ride details are not available
            Log.e("PublishSuccess", "Failed to load ride details")
            // You may want to show an error message to the user here
        }

        continueButton.setOnClickListener {
            // Redirect to the main activity or any other page
            val intent = Intent(this, DriverNavigation::class.java) // Replace with your main activity
            startActivity(intent)
            finish() // Close the success page
        }
    }

    private fun loadRideDetails(): RideInfo? {
        val sharedPreferences = getSharedPreferences("RideDetails", MODE_PRIVATE)


        // Retrieve the user_id from SharedPreferences
        // val userId = 1 // Assuming this is a constant or retrieved differently
        val sharedPreferencesuser = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPreferencesuser.getString("userID", null) ?: "UnknownDriver"


        // Get the rest of the ride details from SharedPreferences
        val startLatitude = sharedPreferences.getFloat("start_latitude", -1f).toDouble()
        val startLongitude = sharedPreferences.getFloat("start_longitude", -1f).toDouble()
        val endLatitude = sharedPreferences.getFloat("end_latitude", -1f).toDouble()
        val endLongitude = sharedPreferences.getFloat("end_longitude", -1f).toDouble()
        val departureDate = sharedPreferences.getString("departure_date", null) ?: return null
        val departureTime = sharedPreferences.getString("departure_time", null) ?: return null
        val availableSeats = sharedPreferences.getInt("available_seats", -1)
        if (availableSeats == -1) return null
        val pricePerSeat = sharedPreferences.getFloat("cost", -1f).toDouble()
        val rideStatus = sharedPreferences.getString("ride_status", "scheduled") ?: "scheduled"
        val carModel = sharedPreferences.getString("car_model", null) ?: return null

        // Retrieve the current ride_counter from SharedPreferences
        val rideCounter = sharedPreferences.getInt("ride_counter", 1)  // Default to 1 or any number you like

        // Increment the ride_counter to generate the new ride_id
        val newRideId = rideCounter + 1

        // Return the new RideInfo object with the generated ride_id
        return RideInfo(
            ride_id = "", // Convert to string to match the RideInfo model
            driver_id = userId,
            start_latitude = startLatitude,
            start_longitude = startLongitude,
            end_latitude = endLatitude,
            end_longitude = endLongitude,
            departure_date = departureDate,
            departure_time = departureTime,
            available_seats = availableSeats,
            price_per_seat = pricePerSeat,
            ride_status = rideStatus,
            car_model = carModel
        )
    }

    private fun saveRideToDatabase(ride: RideInfo) {
        // Convert latitude and longitude to addresses
        ride.start_location = getAddressFromCoordinates(ride.start_latitude, ride.start_longitude)
        ride.end_location = getAddressFromCoordinates(ride.end_latitude, ride.end_longitude)

        // Generate a new key under the "RideInfo" node
        val newRideKey = database.child("RideInfo").push().key
        ride.ride_id = newRideKey ?: "UnknownRideId"

        // Ensure the key is not null
        if (newRideKey != null) {
            // Set the ride data under the new key
            database.child("RideInfo").child(newRideKey).setValue(ride)
                .addOnSuccessListener {
                    Log.d("Database", "Ride data saved successfully")
                }
                .addOnFailureListener {
                    Log.e("Database", "Failed to save ride data", it)
                }
        } else {
            Log.e("Database", "Failed to generate a new ride key")
        }
    }

    private fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this)
        return try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            // If addressList is not null and contains at least one address
            addressList?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
        } catch (e: Exception) {
            Log.e("Geocoder", "Error fetching location address: ${e.message}")
            "Unknown Location"
        }
    }

    private fun sendGmailNotification(recipientEmail: String, rideDetails: RideInfo) {
        val senderEmail = "takmanisha67@gmail.com" // Replace with your Gmail address
        val senderPassword = "jvul sqxx ognp hpjq"     // Replace with your Gmail password or app password

        var subject = "Ride Published Successfully!"
        val messageBody = """
            Dear User,
            
            Your ride has been successfully published with the following details:
            
            - Start Location: ${rideDetails.start_location}
            - End Location: ${rideDetails.end_location}
            - Departure Date: ${rideDetails.departure_date}
            - Departure Time: ${rideDetails.departure_time}
            - Available Seats: ${rideDetails.available_seats}
            - Price Per Seat: ${rideDetails.price_per_seat}

            Thank you for using our service!

            Regards,
            The Corider Team
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val props = Properties()
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.starttls.enable"] = "true"
                props["mail.smtp.host"] = "smtp.gmail.com"
                props["mail.smtp.port"] = "587"

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = subject
                    setText(messageBody)
                }

                Transport.send(message)
                Log.d("GmailNotification", "Email sent successfully to $recipientEmail")
            } catch (e: Exception) {
                Log.e("GmailNotification", "Failed to send email: ${e.message}")
            }
        }


    }
}
