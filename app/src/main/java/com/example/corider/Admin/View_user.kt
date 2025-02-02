package com.example.corider.Admin

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.R
import com.example.corider.model.User
import com.google.firebase.database.*

class View_user : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_user)

        val tableLayout = findViewById<TableLayout>(R.id.tableLayoutUsers)

        // Get reference to Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("user")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tableLayout.removeAllViews() // Clear any previous rows

                // Add header row from XML
                val headerRow = TableRow(this@View_user)
                headerRow.setBackgroundColor(resources.getColor(R.color.teal_700)) // Header color
                headerRow.setPadding(8, 8, 8, 8)

                // User ID
                val userIdHeader = TextView(this@View_user).apply {
                    text = "User ID"
                    setTextColor(resources.getColor(android.R.color.white)) // White text
                    setTypeface(null, Typeface.BOLD) // Use setTypeface to set bold
                    setPadding(16, 16, 16, 16)
                }
                headerRow.addView(userIdHeader)

                // Name
                val nameHeader = TextView(this@View_user).apply {
                    text = "Name"
                    setTextColor(resources.getColor(android.R.color.white))
                    setTypeface(null, Typeface.BOLD) // Use setTypeface to set bold
                    setPadding(16, 16, 16, 16)
                }
                headerRow.addView(nameHeader)

                // Email
                val emailHeader = TextView(this@View_user).apply {
                    text = "Email"
                    setTextColor(resources.getColor(android.R.color.white))
                    setTypeface(null, Typeface.BOLD) // Use setTypeface to set bold
                    setPadding(16, 16, 16, 16)
                }
                headerRow.addView(emailHeader)

                // Action
                val actionHeader = TextView(this@View_user).apply {
                    text = "Action"
                    setTextColor(resources.getColor(android.R.color.white))
                    setTypeface(null, Typeface.BOLD) // Use setTypeface to set bold
                    setPadding(16, 16, 16, 16)
                }
                headerRow.addView(actionHeader)

                tableLayout.addView(headerRow)

                // Loop through all the users in Firebase
                for (userSnapshot in snapshot.children) {
                    // Manually deserialize each user
                    val userId = userSnapshot.child("userId").getValue(Any::class.java)?.toString() ?: ""
                    val userName = userSnapshot.child("userName").getValue(String::class.java) ?: ""
                    val email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                    val userPassword = userSnapshot.child("userPassword").getValue(String::class.java) ?: ""
                    val creationDate = userSnapshot.child("creationDate").getValue(Any::class.java)?.toString() ?: ""

                    // Create User object manually
                    val user = User(userId, userName, email, userPassword, creationDate)

                    // Add row to TableLayout
                    val tableRow = TableRow(this@View_user)
                    tableRow.setPadding(8, 8, 8, 8)

                    // User ID
                    val userIdTextView = TextView(this@View_user)
                    userIdTextView.text = user.userId
                    userIdTextView.setPadding(8, 8, 8, 8)
                    tableRow.addView(userIdTextView)

                    // Name
                    val nameTextView = TextView(this@View_user)
                    nameTextView.text = user.userName
                    nameTextView.setPadding(8, 8, 8, 8)
                    tableRow.addView(nameTextView)

                    // Email
                    val emailTextView = TextView(this@View_user)
                    emailTextView.text = user.email
                    emailTextView.setPadding(8, 8, 8, 8)
                    tableRow.addView(emailTextView)

                    // Delete Button
                    val deleteButton = Button(this@View_user)
                    deleteButton.text = "Delete"
                    deleteButton.setPadding(8, 8, 8, 8)
                    deleteButton.setOnClickListener {
                        // Implement delete logic here
                        deleteUser(user)
                    }
                    tableRow.addView(deleteButton)

                    tableLayout.addView(tableRow)
                }
            }



            override fun onCancelled(error: DatabaseError) {
                // Handle any errors when fetching data
                println("Database error")
            }
        })


    }

    private fun deleteUser(user: User) {
        // Get reference to Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("user")

        // Match userId from the User object with the userId in the database
        myRef.orderByChild("userId").equalTo(user.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // If a match is found, remove that user
                    for (userSnapshot in snapshot.children) {
                        userSnapshot.ref.removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Successfully deleted the user
                                Log.d("View_user", "User deleted: ${user.userName}")
                            } else {
                                // Log the error if the deletion failed
                                Log.e("View_user", "Failed to delete user: ${user.userName}, Error: ${task.exception?.message}")
                            }
                        }
                    }
                } else {
                    // If no user with that userId is found
                    Log.e("View_user", "No user found with userId: ${user.userId}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors
                Log.e("View_user", "Database error: ${error.message}")
            }
        })
    }



}
