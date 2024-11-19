package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth  // Firebase Authentication instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure that the layout file is named activity_login.xml
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Find views by their IDs from the XML layout
        val loginButton: Button = findViewById(R.id.login_button)
        val createAccountTextView: TextView = findViewById(R.id.create_account_text_view)

        // Set click listener for the login button
        loginButton.setOnClickListener {
            loginUser()
        }

        // Set click listener for the "Create an Account" text view
        createAccountTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        // Retrieve email and password from EditText fields
        val email = findViewById<EditText>(R.id.email_edit_text).text.toString().trim()
        val password = findViewById<EditText>(R.id.password_edit_text).text.toString().trim()

        // Check if email and password fields are not empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // Attempt to sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    // Navigate to MainActivity
                    navigateToMainActivity()
                } else {
                    // Sign-in failure
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close LoginActivity so the user cannot go back to it with the back button
    }
}
