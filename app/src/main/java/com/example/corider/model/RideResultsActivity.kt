package com.example.corider.model

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.R


public class RideResultsActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_ride) // Ensure this layout exists and is correct

        webView = findViewById(R.id.webView2)

        // Configure WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = WebViewClient()

        // Add JavaScript interface
        webView.addJavascriptInterface(WebAppBridge(), "AndroidBridge")

        // Load the local HTML file
        webView.loadUrl("file:///android_asset/ride_results.html")

        // Enable WebView debugging (useful for development)
        WebView.setWebContentsDebuggingEnabled(true)
    }

    // JavaScript Interface class
    private inner class WebAppBridge {
        @JavascriptInterface
        fun saveDestinationLocation(latitude: Double, longitude: Double) {
            saveDestinationLocationToPreferences(latitude, longitude)
        }

        @JavascriptInterface
        fun proceedToNextStep() {
            runOnUiThread {
                navigateToSearchRideForm()
            }
        }
    }

    private fun saveDestinationLocationToPreferences(latitude: Double, longitude: Double) {
        destinationLatitude = latitude
        destinationLongitude = longitude

        val sharedPreferences = getSharedPreferences("DestinationPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("destination_latitude", latitude.toFloat())
            putFloat("destination_longitude", longitude.toFloat())
            apply()
        }

        Log.d(
            "RideResultsActivity",
            "Destination location saved: Latitude = $latitude, Longitude = $longitude"
        )
    }

    private fun navigateToSearchRideForm() {
        val intent = Intent(this, SearchRideForm::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Ensure the current activity is removed from the back stack
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
