package com.example.corider.Payment

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.R
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class RazorpayPayment : AppCompatActivity(), PaymentResultListener {

    private lateinit var etAmount: EditText
    private val sharedPrefFile = "BookRideDetails"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_razorpay_payment)

        etAmount = findViewById(R.id.et_amount)

        // Fetch the totalPrice value from the shared preferences
        val sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        val totalPrice = sharedPreferences.getFloat("totalPrice", 0.00f).toString()


        // Set the totalPrice value in the EditText and make it read-only
        etAmount.setText(totalPrice)
        etAmount.isFocusable = false
        etAmount.isClickable = false

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_pay).setOnClickListener {
            startPayment(totalPrice)
        }
    }

    private fun startPayment(amountInRupees: String) {
        val checkout = Checkout()
        val keyID = getString(R.string.razropay_api_key)  // Fetch the key ID from strings.xml
        checkout.setKeyID(keyID)

        // Convert the amount to paisa (1 INR = 100 paisa)
        val amountInPaisa = (amountInRupees.toDouble() * 100).toInt()

        if (amountInPaisa == null || amountInPaisa <= 0) {
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
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_SHORT).show()
    }
}
