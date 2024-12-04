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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SettingsActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var currentPasswordEditText: EditText
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
        currentPasswordEditText = findViewById(R.id.edit_current_password)
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
            val password = currentPasswordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            user?.let {
                reauthenticateAndUpdateProfile(it, name, email, password)
            } ?: run {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun reauthenticateAndUpdateProfile(user: FirebaseUser, name: String, email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        // Reauthenticate the user
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                // Update email
                user.updateEmail(email).addOnCompleteListener { emailUpdateTask ->
                    if (emailUpdateTask.isSuccessful) {
                        Toast.makeText(this, "Email updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = emailUpdateTask.exception?.message ?: "Unknown error"
                        Toast.makeText(this, "Failed to update email: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }

                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user.updateProfile(profileUpdates).addOnCompleteListener { nameUpdateTask ->
                    if (nameUpdateTask.isSuccessful) {
                        Toast.makeText(this, "Name updated successfully!", Toast.LENGTH_SHORT).show()

                        // Firestore 데이터 업데이트 호출
                        updateFirestoreUserData(user.uid, name, email)
                    } else {
                        val errorMessage = nameUpdateTask.exception?.message ?: "Unknown error"
                        Toast.makeText(this, "Failed to update name: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }

                // Update profile picture if selected
                selectedImageUri?.let { uri ->
                    val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
                    storageRef.putFile(uri).addOnCompleteListener { uploadTask ->
                        if (uploadTask.isSuccessful) {
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
            } else {
                val errorMessage = reauthTask.exception?.message ?: "Unknown error"
                Toast.makeText(this, "Reauthentication failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateFirestoreUserData(userId: String, name: String, email: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(userId)

        val updates = mapOf(
            "username" to name,
            "email" to email
        )

        userRef.update(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Firestore data updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                val errorMessage = task.exception?.message ?: "Unknown error"
                Toast.makeText(this, "Failed to update Firestore: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Select from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Set Profile Picture")
            .setItems(options) { _, _ ->
                openGallery()
            }
            .show()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            if (selectedImageUri != null) {
                this.selectedImageUri = selectedImageUri
                profileImageView.setImageURI(selectedImageUri)
            } else {
                Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}