package com.example.corider.User

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.corider.Driver.DriverInbox
import com.example.corider.Driver.DriverProfile
import com.example.corider.Driver.PublishActivity
import com.example.corider.R
import com.google.android.material.navigation.NavigationView

class UserNavigation : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var buttonDrawerToggle: ImageButton
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_navigation)

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Access the included layout using the id
        val includedLayout = findViewById<LinearLayout>(R.id.activity_user_home_page)

        // Find the ImageButton within the included layout
        buttonDrawerToggle = includedLayout.findViewById(R.id.buttondrawerToggle)

        // Set OnClickListener for the ImageButton to open/close the drawer
        buttonDrawerToggle.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Set up listener for navigation menu items
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val id = menuItem.itemId

            // Handle navigation item clicks
            when (id) {
                R.id.BookRide -> {
                    // Example: Redirect to another activity
                    val intent = Intent(this, SearchRideActivity::class.java)
                    startActivity(intent)
                }
                R.id.ViewRides -> {
                    // Redirect to another activity
                    val intent = Intent(this, PublishActivity::class.java)
                    startActivity(intent)
                }
                R.id.Profile -> {
                    // Redirect to another activity
                    val intent = Intent(this, DriverProfile::class.java)
                    startActivity(intent)
                }
                R.id.Inbox -> {
                    // Redirect to another activity
                    val intent = Intent(this, DriverInbox::class.java)
                    startActivity(intent)
                }
            }

            // Close the drawer after item is selected
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Handle back button press to close the drawer if it's open
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
