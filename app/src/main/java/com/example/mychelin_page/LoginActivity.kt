package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            installSplashScreen()
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)
            enableEdgeToEdge()

            // Initialize Firebase Auth and SessionManager
            auth = FirebaseAuth.getInstance()
            sessionManager = SessionManager(this)

            // Check if user is already logged in
            if (sessionManager.isLoggedIn()) {
                val userData = sessionManager.getLoginSession()
                userData[SessionManager.KEY_EMAIL]?.let { email ->
                    val savedPassword = sessionManager.getPassword()
                    if (savedPassword.isNotEmpty()) {
                        loginUser(email, savedPassword)
                        return@onCreate  // 로그인 처리 후 바로 리턴
                    }
                }
            }

            // Initialize views and set up listeners
            initializeViews()
        } catch (e: Exception) {
            Log.e("LoginActivity", "onCreate error", e)
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        val loginButton: Button = findViewById(R.id.login_button)
        val createAccountTextView: TextView = findViewById(R.id.create_account_text_view)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val rememberMeCheckBox: CheckBox = findViewById(R.id.remember_me_checkbox)

        // Set up auto-fill if available
        val savedUserData = sessionManager.getLoginSession()
        emailEditText.setText(savedUserData[SessionManager.KEY_EMAIL])

        if (sessionManager.getPassword().isNotEmpty()) {
            rememberMeCheckBox.isChecked = true
            passwordEditText.setText(sessionManager.getPassword())
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (rememberMeCheckBox.isChecked) {
                sessionManager.savePassword(password)
            } else {
                sessionManager.clearPassword()
            }

            loginUser(email, password)
        }

        createAccountTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Save login session
                    auth.currentUser?.let { user ->
                        sessionManager.saveLoginSession(email, user.uid)
                    }
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    // 로그인 실패시 세션 클리어
                    sessionManager.clearSession()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}