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

    private var userId = "user123"

    private var onImageSelected: ((Uri) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_profile) // Use your actual layout file here

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

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

        bindImageViewUploaderDb(profileImageView, "profile_images/${currentUser?.uid}.jpg", "profileImageUrl")
        bindImageViewUploaderDb(carImageView, "car_images/${currentUser?.uid}_car.jpg", "carImageUrl")

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.travel_preferences,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        travelPreferencesSpinner.adapter = adapter
    }

    private fun loadUserProfile() {
        currentUser = auth.currentUser
        currentUser?.let { user ->
            userId = user.uid
            userName.setText(user.displayName ?: user.email)
            loadImageFromDatabase("profileImageUrl", profileImageView)
            loadImageFromDatabase("carImageUrl", carImageView)

            val userRef = database.child("users").child(user.uid)
            userRef.get().addOnSuccessListener { dataSnapshot ->
                userProfileData(dataSnapshot)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user profile data", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun userProfileData(dataSnapshot: DataSnapshot) {
        val userProfile = dataSnapshot.getValue(UserProfile::class.java)
        userProfile?.let {
            aboutYou.setText(it.about)
            vehicleModel.setText(it.vehicleModel)
            vehicleNumber.setText(it.vehicleNumber)
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
        val selectedSeatCount = when (seatsRadioGroup.checkedRadioButtonId) {
            R.id.radioButton -> 2
            R.id.radioButton2 -> 4
            R.id.radioButton3 -> 5
            R.id.radioButton4 -> 7
            else -> 0
        }

        val userProfile = UserProfile(
            name,
            about,
            vehicleModelText,
            vehicleNumberText,
            selectedSeatCount
        )

        saveProfileData(userProfile)
    }

    private fun saveProfileData(userProfile: UserProfile) {
        database.child("users").child(userId).setValue(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                loadUserProfile()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImageFromDatabase(field: String, imageView: ImageView) {
        currentUser?.let { user ->
            val userRef = database.child("users").child(user.uid)
            userRef.child(field).get().addOnSuccessListener { dataSnapshot ->
                val imageUrl = dataSnapshot.value as? String
                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(imageUrl).into(imageView)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
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

    private fun uploadImageToFirebase(uri: Uri, storagePath: String, databaseField: String) {
        currentUser?.let { user ->
            val imageRef = storageReference.child(storagePath)

            Log.d("DriverProfileActivity", "Uploading image to: ${imageRef.path}")

            imageRef.putFile(uri).addOnSuccessListener {
                Log.d("DriverProfileActivity", "Image uploaded successfully")
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageUrlToDatabase(downloadUri.toString(), databaseField)
                }
            }.addOnFailureListener { exception ->
                Log.e("DriverProfileActivity", "Image upload failed: ${exception.message}")
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String, databaseField: String) {
        currentUser?.let { user ->
            val userRef = database.child("users").child(user.uid)
            userRef.child(databaseField).setValue(imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, "Image URL saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("DriverProfileActivity", "Failed to save image URL: ${exception.message}")
                    Toast.makeText(this, "Failed to save image URL", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
