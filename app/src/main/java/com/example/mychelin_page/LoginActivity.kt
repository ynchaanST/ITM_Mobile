package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isPasswordVisible = false
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            installSplashScreen()
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)
            enableEdgeToEdge()

            // Initialize Firebase Auth and SessionManager
            auth = FirebaseAuth.getInstance()
            sessionManager = SessionManager(this)

            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(" ")  // Firebase 콘솔에서 가져온 웹 클라이언트 ID
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            // Check if user is already logged in
            if (sessionManager.isLoggedIn()) {
                val userData = sessionManager.getLoginSession()
                userData[SessionManager.KEY_EMAIL]?.let { email ->
                    val savedPassword = sessionManager.getPassword()
                    if (savedPassword.isNotEmpty()) {
                        loginUser(email, savedPassword)
                        return@onCreate
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
        val togglePasswordVisibility: ImageButton = findViewById(R.id.toggle_password_visibility)
        val googleSignInButton: Button = findViewById(R.id.google_sign_in_button)

        // Set up auto-fill if available
        val savedUserData = sessionManager.getLoginSession()
        emailEditText.setText(savedUserData[SessionManager.KEY_EMAIL])

        if (sessionManager.getPassword().isNotEmpty()) {
            rememberMeCheckBox.isChecked = true
            passwordEditText.setText(sessionManager.getPassword())
        }

        // Set up password visibility toggle
        togglePasswordVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.transformationMethod = null
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            } else {
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Set up login button
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

        // Set up Google Sign In button
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        createAccountTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        sessionManager.saveLoginSession(it.email ?: "", it.uid)
                    }
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Firebase Google 인증 실패", Toast.LENGTH_SHORT).show()
                }
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
                    auth.currentUser?.let { user ->
                        sessionManager.saveLoginSession(email, user.uid)
                    }
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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