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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage

class SettingsActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var saveChangesButton: Button

    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Initialize UI elements
        profileImageView = findViewById(R.id.profile_image)
        nameEditText = findViewById(R.id.edit_name)
        emailEditText = findViewById(R.id.edit_email)
        saveChangesButton = findViewById(R.id.save_changes_button)

        // Load current user data
        user?.let {
            nameEditText.setText(it.displayName)
            emailEditText.setText(it.email)
            Glide.with(this)
                .load(it.photoUrl)
                .placeholder(R.drawable.default_profile)
                .into(profileImageView)
        }

        // Set up click listener for the profile image
        profileImageView.setOnClickListener {
            showImageSelectionDialog()
        }

        // Save changes button logic
        saveChangesButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            // Validate inputs
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveUserData(name, email)
        }
    }

    // Save user data to Firebase
    private fun saveUserData(name: String, email: String) {
        val user = auth.currentUser

        if (user != null) {
            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Name updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show()
                }
            }

            // Update email
            user.updateEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show()
                }
            }

            // Update profile picture if selected
            selectedImageUri?.let { uri ->
                val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
                storageRef.putFile(uri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val photoUpdate = UserProfileChangeRequest.Builder()
                                .setPhotoUri(downloadUri)
                                .build()

                            user.updateProfile(photoUpdate).addOnCompleteListener { photoTask ->
                                if (photoTask.isSuccessful) {
                                    Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                    }
                }
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