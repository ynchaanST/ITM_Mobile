package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mychelin_page.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI elements
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val loginButton: Button = findViewById(R.id.login_button)
        val createAccountTextView: TextView = findViewById(R.id.create_account_text_view)

        // Set up click listeners
        loginButton.setOnClickListener {
            // Handle login logic here
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Simple validation logic (can be replaced with actual authentication)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Navigate to Home activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close LoginActivity to prevent going back
            } else {
                // Handle invalid input (e.g., show a Toast message)
            }
        }

        createAccountTextView.setOnClickListener {
            // Navigate to Create Account activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}