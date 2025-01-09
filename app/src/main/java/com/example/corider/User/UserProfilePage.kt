package com.example.corider

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
import com.example.corider.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class UserProfilePageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var profileImageView: ImageView
    private lateinit var storageReference: StorageReference
    private lateinit var userName: EditText
    private lateinit var aboutYou: EditText
    private lateinit var travelPreferencesSpinner: Spinner
    private lateinit var ageTextView: TextView

    private var currentUser: FirebaseUser? = null
    private var userId = "user123"

    private var onImageSelected: ((Uri) -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userprofilepage)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        profileImageView = findViewById(R.id.imgProfilePicture)
        userName = findViewById(R.id.edtUserName)
        aboutYou = findViewById(R.id.edtAboutYou)
        travelPreferencesSpinner = findViewById(R.id.txtTravelPreferencesTitle)
        ageTextView = findViewById(R.id.txtAgeTitle)

        loadUserProfile()
        bindImageViewUploaderDb(profileImageView, "profile_images/${currentUser?.uid}.jpg", "profileImageUrl")

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

            // Fetch additional user profile data
            val userRef = database.child("riderprofile").child(user.uid)

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
            aboutYou.setText(it.about)
        }
    }

    private fun setProfileDataChangeListener(userRef: DatabaseReference) {
        userName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                val name = userName.text.toString()
                userRef.child("name").setValue(name)
            }
        })

        val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null

        aboutYou.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                runnable?.let { handler.removeCallbacks(it) }

                runnable = Runnable {
                    val about = aboutYou.text.toString()
                    userRef.child("about").setValue(about)
                        .addOnSuccessListener {
                            Log.d("DatabaseUpdate", "About field updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("DatabaseUpdate", "Failed to update about field", e)
                        }
                }

                handler.postDelayed(runnable!!, 1000)
            }
        })
    }

    private fun loadImageFromDatabase(field: String, imageView: ImageView) {
        currentUser?.let { user ->
            val userRef = database.child("riderprofile").child(user.uid)
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

            Log.d("ProfileFragment", "Uploading image to: ${imageRef.path}")

            imageRef.putFile(uri).addOnSuccessListener {
                Log.d("ProfileFragment", "Image uploaded successfully")
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageUrlToDatabase(downloadUri.toString(), databaseField)
                }
            }.addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Image upload failed: ${exception.message}")
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String, databaseField: String) {
        currentUser?.let { user ->
            val userRef = database.child("riderprofile").child(user.uid)
            userRef.child(databaseField).setValue(imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, "Image URL saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileFragment", "Failed to save image URL: ${exception.message}")
                    Toast.makeText(this, "Failed to save image URL", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
