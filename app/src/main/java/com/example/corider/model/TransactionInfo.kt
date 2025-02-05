package com.example.corider.model

data class TransactionInfo(
    val transactionId: String,          // Unique transaction ID
    val bookingId: String,
    val ride_id : String,// Foreign Key to Booking table
    val userId: String,                 // Foreign Key to User table
    val paymentStatus: String,          // Status of the payment (e.g., "successful", "failed")
    val paymentMethod: String,          // Payment method (e.g., "credit card", "wallet")
    val paymentDate: String,            // Date when the payment was made
    val amountPaid: Double,             // Amount paid for the booking
             // Total price for the ride/booking

)
