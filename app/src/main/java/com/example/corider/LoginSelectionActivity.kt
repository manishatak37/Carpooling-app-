package com.example.corider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.corider.Driver.DriverNavigation
import com.example.corider.User.UserNavigation

class LoginSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_selection)

        // Find the buttons by their IDs
        val btnLoginAsUser = findViewById<Button>(R.id.btnLoginAsUser)
        val btnLoginAsDriver = findViewById<Button>(R.id.btnLoginAsDriver)

        // Set click listeners for buttons
        btnLoginAsUser.setOnClickListener {
            // Navigate to User Login or Dashboard Activity
            val userIntent = Intent(this, UserNavigation::class.java)
            startActivity(userIntent)
        }

        btnLoginAsDriver.setOnClickListener {
            // Navigate to Driver Login or Dashboard Activity
            val driverIntent = Intent(this, DriverNavigation::class.java)
            startActivity(driverIntent)
        }
    }
}
