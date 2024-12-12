package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
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
            // 저장된 이메일로 자동 로그인 시도
            userData[SessionManager.KEY_EMAIL]?.let { email ->
                val savedPassword = sessionManager.getPassword() // 암호화된 비밀번호 가져오기
                if (savedPassword.isNotEmpty()) {
                    loginUser(email, savedPassword)
                    return@onCreate
                }
            }
        }

        // Initialize views
        val loginButton: Button = findViewById(R.id.login_button)
        val createAccountTextView: TextView = findViewById(R.id.create_account_text_view)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val rememberMeCheckBox: CheckBox = findViewById(R.id.remember_me_checkbox)

        // Set up auto-fill if available
        val savedUserData = sessionManager.getLoginSession()
        emailEditText.setText(savedUserData[SessionManager.KEY_EMAIL])

        // 저장된 비밀번호가 있으면 체크박스 체크
        if (sessionManager.getPassword().isNotEmpty()) {
            rememberMeCheckBox.isChecked = true
            passwordEditText.setText(sessionManager.getPassword())
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (rememberMeCheckBox.isChecked) {
                sessionManager.savePassword(password) // 비밀번호 저장
            } else {
                sessionManager.clearPassword() // 비밀번호 삭제
            }

            loginUser(email, password)
        }

        createAccountTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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