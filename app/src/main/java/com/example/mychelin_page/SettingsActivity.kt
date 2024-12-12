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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.concurrent.thread

class SettingsActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private var isUpdating = false
    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        initializeViews()

        // Load current user data
        loadCurrentUserData()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        profileImageView = findViewById(R.id.profile_image)
        nameEditText = findViewById(R.id.edit_name)
        emailEditText = findViewById(R.id.edit_email)
        passwordEditText = findViewById(R.id.edit_password)
    }

    private fun loadCurrentUserData() {
        val user = auth.currentUser
        if (user != null) {
            nameEditText.setText(user.displayName)
            emailEditText.setText(user.email)

            // Load additional data from Firestore
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nameEditText.setText(document.getString("username"))
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .into(profileImageView)
                        }
                    }
                }
        }
    }

    private fun setupClickListeners() {
        profileImageView.setOnClickListener {
            showImageSelectionDialog()
        }

        findViewById<Button>(R.id.save_changes_button).setOnClickListener {
            if (!isUpdating) {
                saveChanges()
            }
        }
    }

    private fun saveChanges() {
        if (!validateInputs()) return

        isUpdating = true
        showProgressDialog()

        val user = auth.currentUser ?: return
        val newName = nameEditText.text.toString()
        val newEmail = emailEditText.text.toString()
        val newPassword = passwordEditText.text.toString()

        // Update Firebase Auth Profile
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                // Update email if changed
                if (newEmail != user.email) {
                    user.updateEmail(newEmail)
                        .addOnSuccessListener {
                            // Update password if provided
                            if (newPassword.isNotEmpty()) {
                                user.updatePassword(newPassword)
                                    .addOnSuccessListener {
                                        updateFirestoreAndImage(user.uid, newName, newEmail)
                                    }
                                    .addOnFailureListener { e ->
                                        handleUpdateError("비밀번호 업데이트 실패: ${e.message}")
                                    }
                            } else {
                                updateFirestoreAndImage(user.uid, newName, newEmail)
                            }
                        }
                        .addOnFailureListener { e ->
                            handleUpdateError("이메일 업데이트 실패: ${e.message}")
                        }
                } else {
                    if (newPassword.isNotEmpty()) {
                        user.updatePassword(newPassword)
                            .addOnSuccessListener {
                                updateFirestoreAndImage(user.uid, newName, newEmail)
                            }
                            .addOnFailureListener { e ->
                                handleUpdateError("비밀번호 업데이트 실패: ${e.message}")
                            }
                    } else {
                        updateFirestoreAndImage(user.uid, newName, newEmail)
                    }
                }
            }
            .addOnFailureListener { e ->
                handleUpdateError("프로필 업데이트 실패: ${e.message}")
            }
    }

    private fun updateFirestoreAndImage(userId: String, name: String, email: String) {
        if (selectedImageUri != null) {
            // Upload new image
            val storageRef = FirebaseStorage.getInstance().reference
                .child("profile_images/$userId.jpg")

            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateFirestoreData(userId, name, email, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    handleUpdateError("이미지 업로드 실패: ${e.message}")
                }
        } else {
            updateFirestoreData(userId, name, email, null)
        }
    }

    private fun updateFirestoreData(userId: String, name: String, email: String, profileImageUrl: String?) {
        val userData = mutableMapOf<String, Any>(
            "username" to name,
            "email" to email
        )

        if (profileImageUrl != null) {
            userData["profileImageUrl"] = profileImageUrl
        }

        db.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                hideProgressDialog()
                isUpdating = false
                Toast.makeText(this, "프로필이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                // Refresh the activity
                finish()
            }
            .addOnFailureListener { e ->
                handleUpdateError("Firestore 업데이트 실패: ${e.message}")
            }
    }

    private fun validateInputs(): Boolean {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (name.isEmpty()) {
            nameEditText.error = "이름을 입력해주세요"
            return false
        }

        if (email.isEmpty()) {
            emailEditText.error = "이메일을 입력해주세요"
            return false
        }

        if (password.isNotEmpty() && password.length < 6) {
            passwordEditText.error = "비밀번호는 6자 이상이어야 합니다"
            return false
        }

        return true
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("갤러리에서 선택")
        AlertDialog.Builder(this)
            .setTitle("프로필 사진 설정")
            .setItems(options) { _, _ ->
                openGallery()
            }
            .show()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(profileImageView)
            }
        }
    }

    private fun showProgressDialog() {
        progressDialog = AlertDialog.Builder(this)
            .setMessage("프로필 업데이트 중...")
            .setCancelable(false)
            .create()
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun handleUpdateError(message: String) {
        hideProgressDialog()
        isUpdating = false
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgressDialog()
    }
}