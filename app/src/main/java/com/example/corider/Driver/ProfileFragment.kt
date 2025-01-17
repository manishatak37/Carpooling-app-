package com.example.corider.Driver

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.corider.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.example.corider.model.UserProfile

class ProfileFragment : AppCompatActivity() {

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
    private lateinit var drivingSkillTextView: TextView
    private lateinit var ageTextView: TextView

    private var currentUser: FirebaseUser? = null
    private var userId = "user123"

    private var onImageSelected: ((Uri) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)  // Use setContentView instead of inflater

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
        drivingSkillTextView = findViewById(R.id.drivingSkillEditText)
        ageTextView = findViewById(R.id.textView15)
        seatsRadioGroup = findViewById(R.id.seats)

        loadUserProfile()

        // Bind image upload functionality
        bindImageViewUploaderDb(profileImageView, "profile_images/${currentUser?.uid}.jpg", "profileImageUrl")
        bindImageViewUploaderDb(carImageView, "car_images/${currentUser?.uid}_car.jpg", "carImageUrl")

        // Set up the travel preferences spinner
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

            // Load images from Firebase
            loadImageFromDatabase("profileImageUrl", profileImageView)
            loadImageFromDatabase("carImageUrl", carImageView)

            // Fetch additional user profile data
            val userRef = database.child("users").child(user.uid)

            // Initial data load
            userRef.get().addOnSuccessListener { dataSnapshot ->
                userProfileData(dataSnapshot)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user profile data", Toast.LENGTH_SHORT).show()
            }

            // Save user profile as the user enters data
            setProfileDataChangeListener(userRef)
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun userProfileData(dataSnapshot: DataSnapshot) {
        val userProfile = dataSnapshot.getValue(UserProfile::class.java)
        userProfile?.let {
            aboutYou.setText(it.about) // EditText for 'About You'
            vehicleModel.setText(it.vehicleModel)
            vehicleNumber.setText(it.vehicleNumber)

            // Set seats based on the retrieved data
            when (it.seats) {
                2 -> seatsRadioGroup.check(R.id.radioButton)
                4 -> seatsRadioGroup.check(R.id.radioButton2)
                5 -> seatsRadioGroup.check(R.id.radioButton3)
                7 -> seatsRadioGroup.check(R.id.radioButton4)
            }
        }
    }

    private fun setProfileDataChangeListener(userRef: DatabaseReference) {
        var handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null

        // Set listeners for text change
        aboutYou.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                runnable?.let {
                    handler.removeCallbacks(it)
                }

                runnable = Runnable {
                    val about = aboutYou.text.toString()
                    userRef.child("about").setValue(about)
                }
                handler.postDelayed(runnable!!, 1000)
            }
        })

        // Add listeners for other fields similarly
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

            Log.d("ProfileActivity", "Uploading image to: ${imageRef.path}")

            imageRef.putFile(uri).addOnSuccessListener {
                Log.d("ProfileActivity", "Image uploaded successfully")
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageUrlToDatabase(downloadUri.toString(), databaseField)
                }
            }.addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Image upload failed: ${exception.message}")
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
                    Log.e("ProfileActivity", "Failed to save image URL: ${exception.message}")
                    Toast.makeText(this, "Failed to save image URL", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
