package com.example.corider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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

        // Navigate to Sign Up Activity
        binding.donthavebutton.setOnClickListener {
            startActivity(Intent(this, SignActivity::class.java))
        }

        // Button to log in
        binding.loginButton.setOnClickListener {
            email = binding.loginemail.text.toString().trim()
            password = binding.loginpassword.text.toString().trim()

            if (email == "admin@gmail.com" && password == "admin") {
                // Redirect to Admin Home Page
                val intent = Intent(this, Admin_home_page::class.java)
                startActivity(intent)
                finish()
            } else if (validateInput()) { // Validate before login
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
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(this, "Google sign-in failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to validate input before login
    private fun validateInput(): Boolean {
        if (email.isEmpty()) {
            binding.loginemail.error = "Email cannot be empty"
            binding.loginemail.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginemail.error = "Invalid email format"
            binding.loginemail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            binding.loginpassword.error = "Password cannot be empty"
            binding.loginpassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            binding.loginpassword.error = "Password must be at least 6 characters long"
            binding.loginpassword.requestFocus()
            return false
        }
        return true
    }

    // Function to sign in user
    private fun signInUser(email: String, password: String) {
        val usersRef = database.child("user")

        usersRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val usersSnapshot = task.result
                var userFound = false

                for (userSnapshot in usersSnapshot.children) {
                    val userEmail = userSnapshot.child("email").getValue(String::class.java)
                    val userPassword = userSnapshot.child("userPassword").getValue(String::class.java)

                    if (userEmail == email && userPassword == password) {
                        val userId = userSnapshot.child("userId").getValue(String::class.java)
                        userFound = true

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("userID", userId)
                        editor.apply()

                        startActivity(Intent(this, LoginSelectionActivity::class.java))
                        finish()
                        break
                    }
                }

                if (!userFound) {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Database error. Try again later.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to navigate to the main activity after successful login
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("userID", user.uid)
            editor.apply()

            startActivity(Intent(this, LoginSelectionActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
