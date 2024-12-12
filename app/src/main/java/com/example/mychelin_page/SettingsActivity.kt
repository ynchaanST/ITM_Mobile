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

            // Validate inputs
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // Update profile information
                updateProfileInfo(name, email, password)

                // Upload profile image if selected
                if (selectedImageUri != null) {
                    uploadImageToFirebase(selectedImageUri!!)
                }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load current profile image from Firebase Storage
    private fun loadProfileImage() {
        val user = auth.currentUser
        if (user != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                profileImageView.setImageURI(uri)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load profile image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Update name, email, and password in Firebase
    private fun updateProfileInfo(name: String, email: String, password: String) {
        val user = auth.currentUser

        if (user != null) {
            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show()
                }

            // Update email
            user.updateEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show()
                }

            // Update password
            user.updatePassword(password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Upload selected image to Firebase Storage
    private fun uploadImageToFirebase(imageUri: Uri) {
        val user = auth.currentUser
        if (user != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                }
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
            } else {
                Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}