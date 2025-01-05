package com.example.corider.Driver

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import androidx.activity.result.contract.ActivityResultContracts
import com.example.corider.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DriverProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImageView: ImageView
    private lateinit var carImageView: ImageView
    private lateinit var userName: EditText
    private lateinit var aboutYou: EditText
    private lateinit var travelPreferencesSpinner: Spinner
    private lateinit var seatsRadioGroup: RadioGroup

    private lateinit var vehicleModel: EditText
    private lateinit var vehicleNumber: EditText
    private lateinit var drivingSkillTextView: EditText
    private lateinit var ageTextView: EditText

    private lateinit var saveButton: Button
    private var currentUser: FirebaseUser? = null
    private lateinit var userId: String

    private var onImageSelected: ((Uri) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_profile) // Use your actual layout file here

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
        userId = getUserIdFromPreferences()  // Placeholder for the user ID (use the actual user ID)

        profileImageView = findViewById(R.id.profileImageView)
        carImageView = findViewById(R.id.imageView4)
        userName = findViewById(R.id.userName)
        aboutYou = findViewById(R.id.aboutYou)
        travelPreferencesSpinner = findViewById(R.id.travelPreferencesSpinner)
        vehicleModel = findViewById(R.id.vehicleModel)
        vehicleNumber = findViewById(R.id.vehicleNumber)

        val drivingSkillTextView: TextView = findViewById(R.id.drivingSkillEditText)
        val ageTextView: TextView = findViewById(R.id.textView15)
        seatsRadioGroup = findViewById(R.id.seats)
        saveButton = findViewById(R.id.saveButton)

        loadUserProfile()
        saveButton.setOnClickListener {
            saveProfile()
        }

        bindImageViewUploaderDb(profileImageView, "profile_images", "profileImageUrl")
        bindImageViewUploaderDb(carImageView, "car_images", "carImageUrl")


        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.travel_preferences,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        travelPreferencesSpinner.adapter = adapter
    }

    private fun loadUserProfile() {
        // Log the userId for debugging
        Log.d("LoadUserProfile", "Using userId: $userId")

        if (!userId.isNullOrEmpty()) {
            // Reference to the 'userProfile' node in Firebase
            val userRef = database.child("userProfile")

            // Query to find the user profile where userId matches
            userRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Log success message
                            Log.d("LoadUserProfile", "User profile found for userId: $userId")

                            // Get the first child as the user profile
                            val userProfileSnapshot = dataSnapshot.children.firstOrNull()
                            if (userProfileSnapshot != null) {

                                // Load the user profile data
                                loadImageFromDatabase("profileImageUrl", profileImageView)
                                loadImageFromDatabase("carImageUrl", carImageView)
                                userProfileData(userProfileSnapshot)

                            } else {
                                Log.e("LoadUserProfile", "No user profile snapshot available for userId: $userId")
                                Toast.makeText(
                                    this@DriverProfile,
                                    "No user profile found. Please create your profile.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Log.e("LoadUserProfile", "No matching profile found for userId: $userId")
                            Toast.makeText(
                                this@DriverProfile,
                                "User profile not found. Please create your profile.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Log the error
                        Log.e("LoadUserProfile", "Error fetching user profile: ${error.message}")
                        Toast.makeText(
                            this@DriverProfile,
                            "Failed to load user profile data.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } else {
            // Log a message when userId is empty or missing
            Log.e("LoadUserProfile", "User ID is empty or not available.")
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }





    private fun userProfileData(dataSnapshot: DataSnapshot) {
        val userProfile = dataSnapshot.getValue(UserProfile::class.java)
        userProfile?.let {
            userName.setText(it.name)          // Populate username field
            aboutYou.setText(it.about)        // Populate about field
            vehicleModel.setText(it.vehicleModel)  // Populate vehicle model field
            vehicleNumber.setText(it.vehicleNumber) // Populate vehicle number field

            // Select the appropriate radio button for the number of seats
            when (it.seats) {
                2 -> seatsRadioGroup.check(R.id.radioButton)
                4 -> seatsRadioGroup.check(R.id.radioButton2)
                5 -> seatsRadioGroup.check(R.id.radioButton3)
                7 -> seatsRadioGroup.check(R.id.radioButton4)
            }
        }
    }


    private fun saveProfile() {
        val name = userName.text.toString()
        val about = aboutYou.text.toString()
        val vehicleModelText = vehicleModel.text.toString()
        val vehicleNumberText = vehicleNumber.text.toString()
        val selectedSeatCount: Int = when (seatsRadioGroup.checkedRadioButtonId) {
            R.id.radioButton -> 2   // 2 seats for radioButton
            R.id.radioButton2 -> 4  // 4 seats for radioButton2
            R.id.radioButton3 -> 5  // 5 seats for radioButton3
            R.id.radioButton4 -> 7  // 7 seats for radioButton4
            else -> 0               // Default case if no radio button is selected
        }

        val userProfile = UserProfile(
            userId = userId,  // Use actual userId
            name = name,
            about = about,
            vehicleModel = vehicleModelText,
            vehicleNumber = vehicleNumberText,
            seats = selectedSeatCount, // This is correctly an Int
            travelPreferences = travelPreferencesSpinner.selectedItem.toString() // Ensure correct string value
        )

        saveProfileData(userProfile)
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        // Provide a default value like empty string or other placeholder if userID is not found
        return sharedPreferences.getString("userID", "") ?: ""
    }

    private fun saveProfileData(userProfile: UserProfile) {
        val userId = getUserIdFromPreferences()

        if (userId != null) {
            // Save profile data to Firebase under the "users" node
            database.child("userProfile").child(userId).setValue(userProfile)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                    loadUserProfile()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }








    private fun bindImageViewUploaderDb(imageView: ImageView, storagePath: String, databaseField: String) {
        imageView.setOnClickListener {
            onImageSelected = { uri ->
                imageView.setImageURI(uri)
                uploadImageToFirebase(uri, storagePath, databaseField)
            }
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerResultLauncher.launch(intent)
    }

    private val imagePickerResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                onImageSelected?.invoke(uri)
            }
        }
    }

    // Upload image to Firebase Storage
    private fun uploadImageToFirebase(uri: Uri, storagePath: String, databaseField: String) {
        val userId = getUserIdFromPreferences()

        if (userId != null) {
            // Construct the storage path using the folder and userId
            val imageRef = storageReference.child("$storagePath/$userId.jpg")

            Log.d("DriverProfileActivity", "Uploading image to: ${imageRef.path}")

            // Upload the file to Firebase Storage
            imageRef.putFile(uri).addOnSuccessListener {
                Log.d("DriverProfileActivity", "Image uploaded successfully")

                // After successful upload, get the download URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save the download URL to the database
                    saveImageUrlToDatabase(downloadUri.toString(), databaseField)
                }
            }.addOnFailureListener { exception ->
                Log.e("DriverProfileActivity", "Image upload failed: ${exception.message}")
                Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("DriverProfileActivity", "User ID is null")
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
        }
    }

    // Save image URL to Firebase Database
    private fun saveImageUrlToDatabase(imageUrl: String, databaseField: String) {
        val userId = getUserIdFromPreferences()

        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("userProfile").child(userId)
            userRef.child(databaseField).setValue(imageUrl)
                .addOnSuccessListener {
                    Log.d("DriverProfileActivity", "Image URL saved to database")
                }
                .addOnFailureListener {
                    Log.e("DriverProfileActivity", "Failed to save image URL to database")
                }
        }
    }


    // Load image from Firebase Storage
    private fun loadImageFromDatabase(field: String, imageView: ImageView) {
        val userId = getUserIdFromPreferences()

        if (userId != null) {
            // Choose the correct folder based on the field (profile_images or car_images)
            val storagePath = if (field == "profileImageUrl") "profile_images" else "car_images"
            val imageRef = storageReference.child("$storagePath/$userId.jpg")

            // Retrieve the image from Firebase Storage
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Picasso.get().load(downloadUri).into(imageView)
            }.addOnFailureListener {
                Log.e("DriverProfileActivity", "Failed to load image: ${it.message}")
            }
        }
    }



}
