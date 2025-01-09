package com.example.corider.Fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.corider.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import androidx.activity.result.contract.ActivityResultContracts
import com.example.corider.model.UserProfile

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImageView: ImageView
    private lateinit var carImageView: ImageView
    private lateinit var userName: EditText
    private lateinit var aboutYou: EditText // Changed to EditText
    private lateinit var travelPreferencesSpinner: Spinner
    private lateinit var seatsRadioGroup: RadioGroup

    private lateinit var vehicleModel: EditText
    private lateinit var vehicleNumber: EditText
    private lateinit var drivingSkillTextView: TextView// Updated to EditText
    private lateinit var ageTextView: TextView

    private var currentUser: FirebaseUser? = null
    private var userId = "user123"

    private var onImageSelected: ((Uri) -> Unit)? = null

    // Store a reference to the image selection callback


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        profileImageView = view.findViewById(R.id.profileImageView)
        carImageView = view.findViewById(R.id.imageView4)
        userName = view.findViewById(R.id.userName) // Correct if it's EditText

        aboutYou = view.findViewById(R.id.aboutYou) // Updated to EditText
        travelPreferencesSpinner = view.findViewById(R.id.travelPreferencesSpinner)

        vehicleModel = view.findViewById(R.id.vehicleModel)
        vehicleNumber = view.findViewById(R.id.vehicleNumber)
        drivingSkillTextView = view.findViewById(R.id.drivingSkillEditText) // Updated to EditText
        ageTextView = view.findViewById(R.id.textView15)
        seatsRadioGroup = view.findViewById(R.id.seats)

        loadUserProfile()

        // Bind image upload functionality
        bindImageViewUploaderDb(profileImageView, "profile_images/${currentUser?.uid}.jpg", "profileImageUrl")
        bindImageViewUploaderDb(carImageView, "car_images/${currentUser?.uid}_car.jpg", "carImageUrl")

        // Set up the travel preferences spinner
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.travel_preferences,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        travelPreferencesSpinner.adapter = adapter

        return view
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

            // Set real-time listener to automatically save changes
//            userRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    userProfileData(dataSnapshot)
//                }
//
//                override fun onCancelled(databaseError: DatabaseError) {
//                    Log.e("ProfileFragment", "Failed to read user data: ${databaseError.message}")
//                }
//            })

            // Initial data load
            userRef.get().addOnSuccessListener { dataSnapshot ->
                userProfileData(dataSnapshot)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user profile data", Toast.LENGTH_SHORT).show()
            }

            // Save user profile as the user enters data
            setProfileDataChangeListener(userRef)
        } ?: run {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
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
        // Use proper TextWatcher implementation
        userName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                val name = userName.text.toString()
                userRef.child("name").setValue(name)
            }
        })

//        aboutYou.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(editable: Editable?) {
//                val about = aboutYou.text.toString()
//                userRef.child("about").setValue(about)
//            }
//        })

        // Add a TextWatcher to the "aboutYou" EditText
        // Add a TextWatcher to the "aboutYou" EditText
        var handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null

        aboutYou.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // No action required
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // No action required
            }

            override fun afterTextChanged(editable: Editable?) {
                // If a runnable already exists, remove it to reset the delay time
                runnable?.let {
                    handler.removeCallbacks(it)
                }

                // Create a new runnable to update the database after a short delay
                runnable = Runnable {
                    val about = aboutYou.text.toString()

                    // Update the database with the value after the user stops typing for 500ms
                    userRef.child("about").setValue(about)
                        .addOnSuccessListener {
                            Log.d("DatabaseUpdate", "About field updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("DatabaseUpdate", "Failed to update about field", e)
                        }
                }

                // Delay the database update by 500ms (debouncing the text change)
                handler.postDelayed(runnable!!, 1000)
            }
        })




        // For vehicleModel EditText

        var vehicleModelRunnable: Runnable? = null
        var vehicleNumberRunnable: Runnable? = null

// For vehicleModel EditText
        vehicleModel.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // No action required
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // No action required
            }

            override fun afterTextChanged(editable: Editable?) {
                // If a runnable already exists, remove it to reset the delay time
                vehicleModelRunnable?.let {
                    handler.removeCallbacks(it)
                }

                // Create a new runnable to update the database after a short delay
                vehicleModelRunnable = Runnable {
                    val vehicleModelText = vehicleModel.text.toString()

                    // Update the database with the value after the user stops typing for 500ms
                    userRef.child("vehicleModel").setValue(vehicleModelText)
                        .addOnSuccessListener {
                            Log.d("DatabaseUpdate", "Vehicle model updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("DatabaseUpdate", "Failed to update vehicle model", e)
                        }
                }

                // Delay the database update by 500ms (debouncing the text change)
                handler.postDelayed(vehicleModelRunnable!!, 500)
            }
        })

// For vehicleNumber EditText
        vehicleNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // No action required
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // No action required
            }

            override fun afterTextChanged(editable: Editable?) {
                // If a runnable already exists, remove it to reset the delay time
                vehicleNumberRunnable?.let {
                    handler.removeCallbacks(it)
                }

                // Create a new runnable to update the database after a short delay
                vehicleNumberRunnable = Runnable {
                    val vehicleNumberText = vehicleNumber.text.toString()

                    // Update the database with the value after the user stops typing for 500ms
                    userRef.child("vehicleNumber").setValue(vehicleNumberText)
                        .addOnSuccessListener {
                            Log.d("DatabaseUpdate", "Vehicle number updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("DatabaseUpdate", "Failed to update vehicle number", e)
                        }
                }

                // Delay the database update by 500ms (debouncing the text change)
                handler.postDelayed(vehicleNumberRunnable!!, 500)
            }
        })

        seatsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedSeatCount = when (checkedId) {
                R.id.radioButton -> 2
                R.id.radioButton2 -> 4
                R.id.radioButton3 -> 5
                R.id.radioButton4 -> 7
                else -> 0
            }
            userRef.child("seats").setValue(selectedSeatCount)
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
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String, databaseField: String) {
        currentUser?.let { user ->
            val userRef = database.child("users").child(user.uid)
            userRef.child(databaseField).setValue(imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Image URL saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileFragment", "Failed to save image URL: ${exception.message}")
                    Toast.makeText(requireContext(), "Failed to save image URL", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
