package com.example.corider.model

data class TransactionInfo(
    val transaction_id: String? = null,
    val user_id: String? = null,
    val ride_id: String? = null,
    val amount: Double? = null,
    val transaction_date: String? = null,
    val payment_method: String? = null,
    val transaction_status: String? = null
)
