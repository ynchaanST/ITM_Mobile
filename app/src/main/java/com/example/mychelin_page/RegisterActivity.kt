package com.example.mychelin_page

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var profilePicture: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var alreadyHaveAccountTextView: TextView

    private var currentPhotoPath: String? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        profilePicture = findViewById(R.id.profile_picture)
        nameEditText = findViewById(R.id.name_edit_text)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        registerButton = findViewById(R.id.register_button)
        alreadyHaveAccountTextView = findViewById(R.id.already_have_account_text_view)

        registerButton.setOnClickListener { registerUser() }

        alreadyHaveAccountTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        profilePicture.setOnClickListener { showImageSelectionDialog() }
    }

    private fun registerUser() {
        val username = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Firebase Authentication의 displayName 업데이트
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                                    navigateToMainActivity()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "프로필 업데이트 실패: ${updateTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "회원가입 실패: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("갤러리에서 선택", "사진 촬영")
        AlertDialog.Builder(this)
            .setTitle("프로필 사진 설정")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            try {
                val photoFile = createImageFile()
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    photoFile
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(cameraIntent)
            } catch (ex: IOException) {
                Toast.makeText(this, "파일 생성 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir!!).apply {
            currentPhotoPath = absolutePath
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    profilePicture.setImageURI(selectedImageUri)
                } else {
                    Toast.makeText(this, "이미지 선택 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                currentPhotoPath?.let {
                    val imageFile = File(it)
                    selectedImageUri = Uri.fromFile(imageFile)
                    profilePicture.setImageURI(selectedImageUri)
                } ?: run {
                    Toast.makeText(this, "사진 촬영 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
