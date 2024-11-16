package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mychelin_page.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI elements
        val nameEditText: EditText = findViewById(R.id.name_edit_text)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val registerButton: Button = findViewById(R.id.register_button)
        val alreadyHaveAccountTextView: TextView = findViewById(R.id.already_have_account_text_view)

        // Set up click listeners
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
                // TODO: Show error message (e.g., using a Toast)
            }
        }

        alreadyHaveAccountTextView.setOnClickListener {
            // Navigate back to Login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close RegisterActivity to prevent going back
        }
    }
}