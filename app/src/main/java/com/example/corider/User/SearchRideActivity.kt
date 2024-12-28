package com.example.corider.User




import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.R

class SearchRideActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var pickupLatitude: Double? = null
    private var pickupLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_ride) // Ensure this matches your layout file name

        webView = findViewById(R.id.webView2)
        webView.webViewClient = WebViewClient()

        // Enable JavaScript
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Add JavaScript interface
        webView.addJavascriptInterface(WebAppBridge(this), "AndroidBridge")

        // Load the local HTML file
        webView.loadUrl("file:///android_asset/search_ride.html")

        // Enable debugging (optional)
        WebView.setWebContentsDebuggingEnabled(true)
    }

    // JavaScript Interface class
    private inner class WebAppBridge(private val activity: SearchRideActivity) {
        @JavascriptInterface
        fun savePickupLocation(latitude: Double, longitude: Double) {
            activity.savePickupLocation(latitude, longitude)
        }

        @JavascriptInterface
        fun navigateToResults() {
            val intent = Intent(activity, RideResultsActivity::class.java) // Ensure this is your actual destination Activity
            activity.startActivity(intent)
        }
    }

    private fun savePickupLocation(latitude: Double, longitude: Double) {
        pickupLatitude = latitude
        pickupLongitude = longitude

        // Save location to SharedPreferences
        val sharedPreferences = getSharedPreferences("SearchRidePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("pickup_latitude", latitude.toFloat())
            putFloat("pickup_longitude", longitude.toFloat())
            apply() // Commit changes
        }

        Log.d("SearchRideActivity", "Pickup location saved: Latitude = $pickupLatitude, Longitude = $pickupLongitude")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}