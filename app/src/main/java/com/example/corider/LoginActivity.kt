package com.example.corider


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.corider.Admin.Admin_home_page
import com.example.corider.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Button to navigate to Sign Up Activity
        binding.donthavebutton.setOnClickListener {
            startActivity(Intent(this, SignActivity::class.java))
        }

        // Button to log in
        binding.loginButton.setOnClickListener {
            email = binding.loginemail.text.toString().trim()
            password = binding.loginpassword.text.toString().trim()

            if (validateInput()) {
                signInUser(email, password)
            }
        }

        // Google sign-in button listener
        binding.createButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle Google Sign-In result
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginActivity", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    // Function to validate input
    private fun validateInput(): Boolean {
        return when {
            email.isEmpty() -> {
                binding.loginemail.error = "Email cannot be empty"
                binding.loginemail.requestFocus()
                false
            }
            password.isEmpty() -> {
                binding.loginpassword.error = "Password cannot be empty"
                binding.loginpassword.requestFocus()
                false
            }
            else -> true
        }
    }


    // Function to sign in user
    private fun signInUser(email: String, password: String) {
        // Reference to the "user" node in Firebase
        val usersRef = database.child("user")

// Query to get all users
        usersRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val usersSnapshot = task.result

                // Iterate over each user in the database
                var userFound = false
                for (userSnapshot in usersSnapshot.children) {
                    val userEmail = userSnapshot.child("email").getValue(String::class.java)
                    val userPassword = userSnapshot.child("userPassword").getValue(String::class.java)

                    // Check if email and password match
                    if (userEmail == email && userPassword == password) {
                        val userId = userSnapshot.child("userId").getValue(String::class.java)
                        userFound = true

                        // Store userId in SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("userID", userId)
                        editor.apply()
                        Log.d("LoginActivity", "User ID: ${userId}")

                        // Navigate to Main Activity
                        startActivity(Intent(this, LoginSelectionActivity::class.java))
                        finish()
                        break
                    }
                }

                if (!userFound) {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("LoginActivity", "Error getting users", task.exception)
            }
        }

    }


    // Function to navigate to the main activity after successful login
    private fun updateUI(user: FirebaseUser?) {
        Log.d("LoginActivity", "User ID: ${user?.uid}")
        user?.let {
            // Store user ID in SharedPreferences
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("userID", user.uid)
            editor.apply()

            // Navigate to MainActivity
            startActivity(Intent(this, LoginSelectionActivity::class.java))
            finish() // Optional: Prevent user from returning to login page using back button
        }
    }

}
