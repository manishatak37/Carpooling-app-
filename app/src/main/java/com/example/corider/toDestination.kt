package com.example.corider // Replace with your actual package name

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.*

class toDestination : AppCompatActivity() { // Class name should start with an uppercase letter
    private lateinit var webView: WebView
    private var endLatitude: Double? = null
    private var endLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_destination)

        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()

        // Enable JavaScript
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Add JavaScript interface
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")

        // Load the local HTML file
        webView.loadUrl("file:///android_asset/index2.html")

        // Enable debugging (optional)
        WebView.setWebContentsDebuggingEnabled(true)
    }

    // JavaScript Interface class
    private inner class WebAppInterface(private val activity: toDestination) {

        @JavascriptInterface
        fun storeEndLocation(lat: Double, lon: Double) {
            activity.storeEndLocation(lat, lon)
        }

        @JavascriptInterface
        fun goToNextActivity() {
            val intent = Intent(activity, RidePublishForm::class.java) // Ensure this is your actual Activity name
            activity.startActivity(intent)
        }
    }

    private fun storeEndLocation(lat: Double, lon: Double) {
        endLatitude = lat
        endLongitude = lon

        // Save end location to SharedPreferences
        val sharedPreferences = getSharedPreferences("RideDetails", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("end_latitude", lat.toFloat())
            putFloat("end_longitude", lon.toFloat())
            apply()
        }

        Log.d("ToDestinationActivity", "End Location stored: Latitude = $endLatitude, Longitude = $endLongitude")
    }

    // Handle back navigation
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
