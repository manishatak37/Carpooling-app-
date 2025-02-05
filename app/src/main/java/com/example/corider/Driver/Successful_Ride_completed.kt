package com.example.corider.Driver

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.corider.R
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class Successful_Ride_completed : AppCompatActivity() {

    private lateinit var ride_id: String
    private var destinationLatitude: Double = 0.0
    private var destinationLongitude: Double = 0.0
    private lateinit var rideDate: String
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_successful_ride_completed)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ride_id = intent.getStringExtra("ride_id") ?: ""
        val destination = intent.getStringExtra("destination") ?: ""
        rideDate = intent.getStringExtra("rideDate") ?: ""

        val locationParts = destination.split(",")
        if (locationParts.size == 2) {
            destinationLatitude = locationParts[0].toDoubleOrNull() ?: 0.0
            destinationLongitude = locationParts[1].toDoubleOrNull() ?: 0.0
        }

        if (checkLocationPermission()) {
            proceedWithRide()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            proceedWithRide()
        } else {
            Toast.makeText(this, "Permission denied. Unable to access location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun proceedWithRide() {
        if (isRideToday()) {
//            getCurrentLocation()
            updateRideStatusToCompleted()
        } else {
            Toast.makeText(this, "Ride conditions are not met for completion.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DriverDisplayRide::class.java)
            startActivity(intent)
        }
    }

    private fun isRideToday(): Boolean {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return rideDate == currentDate
    }

    private fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                    return
                }

                if (!isGooglePlayServicesAvailable()) return

                val locationRequest = LocationRequest.create().apply {
                    interval = 10000
                    fastestInterval = 5000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        handleLocationResult(locationResult)
                    }
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } else {
                Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("Successful_Ride", "Security Exception: ${e.message}")
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (status != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Google Play Services required for location", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun handleLocationResult(locationResult: LocationResult?) {
        locationResult?.let {
            val location = it.lastLocation
            if (location != null) {
                val currentLocation = Location("provider").apply {
                    latitude = location.latitude
                    longitude = location.longitude
                }

                val destinationLocation = Location("provider").apply {
                    latitude = destinationLatitude
                    longitude = destinationLongitude
                }

                val distance = currentLocation.distanceTo(destinationLocation)

                if (distance <= 5000) {
                    updateRideStatusToCompleted()
                } else {
                    Toast.makeText(this, "Current location is too far from the destination.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Unable to retrieve current location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRideStatusToCompleted() {
        // Ensure the ride_id is valid
        if (ride_id.isNotEmpty()) {
            val rideRef = database.child("RideInfo").child(ride_id)

            // Update ride_status to "completed"
            rideRef.child("ride_status").setValue("completed")
                .addOnSuccessListener {
                    Toast.makeText(this, "Ride marked as completed!", Toast.LENGTH_SHORT).show()

                    // Navigate back to DriverDisplayRide if needed
                    val intent = Intent(this, DriverDisplayRide::class.java)
                    intent.putExtra("rideId", ride_id)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update ride status.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid ride ID!", Toast.LENGTH_SHORT).show()
        }
    }

}
