package com.example.mychelin_page

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.mychelin_page.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI elements
        val nameEditText: EditText = findViewById(R.id.name_edit_text)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val registerButton: Button = findViewById(R.id.register_button)
        val alreadyHaveAccountTextView: TextView = findViewById(R.id.already_have_account_text_view)
        profileImageView = findViewById(R.id.profile_picture) // 프로필 사진

        // Set up click listeners
        profileImageView.setOnClickListener {
            showImageSelectionDialog() // 프로필 사진 설정 다이얼로그 표시
        }

        registerButton.setOnClickListener {
            // Handle registration logic here
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Basic validation
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // TODO: Implement actual registration logic (e.g., saving user data or server communication)
                // For now, just navigate to Home screen or show a success message
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        alreadyHaveAccountTextView.setOnClickListener {
            // Navigate back to Login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close RegisterActivity to prevent going back
        }
    }

    // Show dialog to choose between gallery and camera
    private fun showImageSelectionDialog() {
        val options = arrayOf("Select from Gallery", "Take a Photo")
        AlertDialog.Builder(this)
            .setTitle("Set Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    // Open gallery to pick an image
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    // Open camera to take a photo
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            try {
                val photoFile = createImageFile()
                val photoURI = FileProvider.getUriForFile(this, "com.example.mychelin_page.fileprovider", photoFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(cameraIntent)
            } catch (ex: IOException) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Create an image file for storing the camera photo
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Handle result from gallery
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri)
            } else {
                Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle result from camera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoPath?.let {
                val imageUri = Uri.fromFile(File(it))
                profileImageView.setImageURI(imageUri)
            } ?: run {
                Toast.makeText(this, "Photo capture failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}