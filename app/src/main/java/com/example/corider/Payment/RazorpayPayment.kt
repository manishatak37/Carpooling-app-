package com.example.corider.Payment

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.Driver.DriverDisplayRide
import com.example.corider.R
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import com.example.corider.model.TransactionInfo
import com.example.corider.User.ViewBookingActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.razorpay.AutoReadOtpHelper

class RazorpayPayment : AppCompatActivity(), PaymentResultListener {

    private lateinit var etAmount: EditText
    private val sharedPrefFile = "BookRideDetails"
    private lateinit var database: DatabaseReference
//    private var otpReceiver: AutoReadOtpHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_razorpay_payment)
//        otpReceiver = AutoReadOtpHelper()

        etAmount = findViewById(R.id.et_amount)

        // Fetch the totalPrice value from the shared preferences
        val sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        val totalPrice = sharedPreferences.getFloat("totalPrice", 0.00f).toString()

        // Set the totalPrice value in the EditText and make it read-only
        etAmount.setText(totalPrice)
        etAmount.isFocusable = false
        etAmount.isClickable = false

        // Get rideId and userId from the Intent
        val rideId = intent.getStringExtra("rideId") ?: ""
        val userId = intent.getStringExtra("userId") ?: ""

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_pay).setOnClickListener {
            startPayment(totalPrice, rideId, userId)
        }
    }

    private fun startPayment(amountInRupees: String, rideId: String, userId: String) {
        val checkout = Checkout()
        val keyID = getString(R.string.razropay_api_key)  // Fetch the key ID from strings.xml
        checkout.setKeyID(keyID)

        // Convert the amount to paisa (1 INR = 100 paisa)
        val amountInPaisa = (amountInRupees.toDouble() * 100).toInt()

        if (amountInPaisa <= 0) {
            Toast.makeText(this, "Invalid amount stored in preferences", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val options = JSONObject()
            options.put("name", "Corider")
            options.put("description", "Test Payment")
            options.put("currency", "INR")
            options.put("amount", amountInPaisa) // Amount in paisa

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Booking successful!", Toast.LENGTH_LONG).show()

        // Generate bookingId and transactionId
        val bookingId = generateBookingId()
        val transactionId = razorpayPaymentID ?: "N/A"

        // Get the amount paid from shared preferences
        val sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        val amountPaid = sharedPreferences.getFloat("totalPrice", 0.00f).toDouble()

        // Get current date for paymentDate
        val paymentDate = getCurrentDate()

        // Create the TransactionInfo object
        val transactionInfo = TransactionInfo(
            transactionId = transactionId,
            bookingId = bookingId,
            ride_id = intent.getStringExtra("ride_id") ?: "",
            userId = intent.getStringExtra("user_id") ?: "",
            paymentStatus = "successful",
            paymentMethod = "razorpay",
            paymentDate = paymentDate,
            amountPaid = amountPaid
        )

        // Post transaction details to server or database
        postTransactionDetails(transactionInfo)

        // Update the booking status in the booking table

    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun generateBookingId(): String {
        // Generate a unique booking ID using a timestamp and UUID
        val timestamp = System.currentTimeMillis()
        val uniqueId = UUID.randomUUID().toString()
        return "BOOKING_${timestamp}_$uniqueId"
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        otpReceiver?.let {
//            try {
//                unregisterReceiver(it)
//            } catch (e: IllegalArgumentException) {
//                // Ignore if receiver was already unregistered
//            }
//        }
//    }


    private fun postTransactionDetails(transactionInfo: TransactionInfo) {
        // Initialize the Firebase database reference
        val database = FirebaseDatabase.getInstance().reference

        // Create a unique ID for the transaction using the Firebase push() method
        val transactionRef = database.child("transactions").push()

        // Set the transaction data to the database
        transactionRef.setValue(transactionInfo)
            .addOnSuccessListener {
                // Transaction details saved successfully
                Toast.makeText(this, "Transaction details saved!", Toast.LENGTH_SHORT).show()
                updateBookingStatus(transactionInfo.ride_id, "successful")
            }
            .addOnFailureListener { exception ->
                // If saving the transaction details fails
                Toast.makeText(this, "Failed to save transaction details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateBookingStatus(ride_id: String, status: String) {
        val rideRef = database.child("bookings").child(ride_id)
        rideRef.child("bookingStatus").setValue(status)
            .addOnSuccessListener {
                // Display success message for transaction
                Toast.makeText(this, "Transaction Successful!", Toast.LENGTH_SHORT).show()

                // Display booking success message
                Toast.makeText(this, "Booking marked as successful!", Toast.LENGTH_SHORT).show()

                // Redirect to ViewBooking page
                val intent = Intent(this, ViewBookingActivity::class.java)
                intent.putExtra("rideId", ride_id)  // Pass rideId to the next activity
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                // Display error message if updating ride status fails
                Toast.makeText(this, "Failed to update ride status.", Toast.LENGTH_SHORT).show()
            }
    }
}

