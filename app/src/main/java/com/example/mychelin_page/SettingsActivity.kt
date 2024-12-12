package com.example.mychelin_page

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SettingsActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        enableEdgeToEdge()

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        profileImageView = findViewById(R.id.profile_image)
        val saveChangesButton: Button = findViewById(R.id.save_changes_button)
        val nameEditText: EditText = findViewById(R.id.edit_name)
        val emailEditText: EditText = findViewById(R.id.edit_email)
        val passwordEditText: EditText = findViewById(R.id.edit_password)

        // Load current profile image
        loadProfileImage()

        // Set up click listener for the profile image
        profileImageView.setOnClickListener {
            showImageSelectionDialog()
        }

        // Save changes button logic
        saveChangesButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Update profile information
            updateProfileInfo(name, email)

            // Update password only if provided
            if (password.isNotEmpty()) {
                updatePassword(password)
            }

            // Upload profile image if selected
            if (selectedImageUri != null) {
                uploadImageToFirebase(selectedImageUri!!)
            }
        }
    }

    // Load current profile image from Firebase Storage
    private fun loadProfileImage() {
        val user = auth.currentUser
        if (user != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(profileImageView)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load profile image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Update name and email in Firebase
    private fun updateProfileInfo(name: String, email: String) {
        val user = auth.currentUser
        if (user != null) {
            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    updateFirestore("name", name)
                }

            // Update email
            user.updateEmail(email)
                .addOnSuccessListener {
                    updateFirestore("email", email)
                }
        }
    }

    // Update password in Firebase
    private fun updatePassword(password: String) {
        val user = auth.currentUser
        user?.updatePassword(password)
    }

    // Update Firestore user data
    private fun updateFirestore(field: String, value: String) {
        val user = auth.currentUser
        if (user != null) {
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            userDoc.update(field, value)
        }
    }

    // Upload selected image to Firebase Storage
    private fun uploadImageToFirebase(imageUri: Uri) {
        val user = auth.currentUser
        if (user != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.putFile(imageUri)
        }
    }

    // Show a dialog to choose from gallery
    private fun showImageSelectionDialog() {
        val options = arrayOf("Select from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Set Profile Picture")
            .setItems(options) { _, _ ->
                openGallery()
            }
            .show()
    }

    // Open the gallery to pick an image
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    // Handle result from gallery
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri)
            }
        }
    }
}