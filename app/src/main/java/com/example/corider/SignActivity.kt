package com.example.corider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.databinding.ActivitySignBinding
import com.example.corider.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import android.content.Context
import android.content.SharedPreferences
import com.example.corider.model.User
import java.text.SimpleDateFormat
import java.util.Date

class SignActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var userPassword: String
    private lateinit var userName: String
    private lateinit var database: DatabaseReference

    // Define a request code for Google Sign-In
    private val RC_SIGN_IN = 9001

    private val binding: ActivitySignBinding by lazy {
        ActivitySignBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize Firebase Database
        database = Firebase.database.reference

        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Set up the Google Sign-In client
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.createButton.setOnClickListener {
            userName = binding.userName.text.toString().trim()
            email = binding.loginemail.text.toString().trim()
            userPassword = binding.password.text.toString().trim()

            // Validate input fields
            if (userName.isBlank() || email.isBlank() || userPassword.isBlank()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
            } else if (userPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                checkIfEmailExists(email, userPassword)
            }
        }

        // Google Sign-In button listener
        binding.button2.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // Handle the result of the Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // Function to check if email already exists
    private fun checkIfEmailExists(email: String, userPassword: String) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods?.isEmpty() == true) {
                    // Email does not exist, create new account
                    createAccount(email, userPassword)
                } else {
                    // Email already exists
                    Toast.makeText(this, "Email is already registered!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error checking email existence", Toast.LENGTH_SHORT).show()
                Log.d("EmailCheck", "checkIfEmailExists: Failure", task.exception)
            }
        }
    }

    // Function to create a new account
    private fun createAccount(email: String, userPassword: String) {
        auth.createUserWithEmailAndPassword(email, userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account created Successfully!", Toast.LENGTH_SHORT).show()
                saveUserData(this)

                // Set the display name for the user in Firebase
                auth.currentUser?.let { user ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build()
                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("Profile", "User profile updated.")
                            }
                        }
                }

                // Redirect to login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Account creation Failed!", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure", task.exception)
            }
        }
    }

    // Function to save user data to Firebase Database

    private fun saveUserData(context: Context) {
        val userName = binding.userName.text.toString().trim()
        val email = binding.loginemail.text.toString().trim()
        val userPassword = binding.password.text.toString().trim() // Assuming there's a field for password

        // Initialize SharedPreferences
        val sharedPrefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()

        // Get the last used user ID and increment it
        var lastUserId = sharedPrefs.getInt("last_user_id", 0)
        val newUserId = lastUserId + 1

        // Ensure currentUser is not null before using it
        auth.currentUser?.let { user ->
            val creationDate: String = SimpleDateFormat("yyyy-MM-dd").format(Date())


            val userModel = User(newUserId, userName, email, userPassword, creationDate)

            // Get a unique key for the new user entry
            val newUserKey = database.child("user").push().key

            newUserKey?.let { key ->
                database.child("user").child(key).setValue(userModel)
                    .addOnSuccessListener {
                        Log.d("Database", "User data saved successfully")
                        // Save the new user ID back to SharedPreferences
                        editor.putInt("last_user_id", newUserId)
                        editor.apply()
                    }
                    .addOnFailureListener {
                        Log.d("Database", "Failed to save user data", it)
                    }
            } ?: Log.d("Database", "Failed to generate unique key for user")
        }
    }

    // Handle the Google Sign-In result
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            // Google Sign-In was successful, authenticate with Firebase
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            // Google Sign-In failed
            Log.w("GoogleSignIn", "Google sign in failed", e)
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Authenticate with Firebase using Google credentials
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    saveUserData(this)  // Save user data if needed
                    // Redirect to home or another activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign-in failed
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}