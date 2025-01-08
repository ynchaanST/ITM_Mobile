package com.example.mychelin_page

import android.content.Context

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    private val encryption = EncryptionUtils() // 암호화 유틸리티 클래스

    companion object {
        const val IS_LOGIN = "IsLoggedIn"
        const val KEY_EMAIL = "email"
        const val KEY_UID = "uid"
        private const val KEY_PASSWORD = "password"
    }

    fun saveLoginSession(email: String, uid: String) {
        editor.apply {
            putBoolean(IS_LOGIN, true)
            putString(KEY_EMAIL, email)
            putString(KEY_UID, uid)
            apply()
        }
    }

    fun savePassword(password: String) {
        val encryptedPassword = encryption.encrypt(password) // 비밀번호 암호화
        editor.apply {
            putString(KEY_PASSWORD, encryptedPassword)
            apply()
        }
    }

    fun getPassword(): String {
        val encryptedPassword = sharedPreferences.getString(KEY_PASSWORD, "") ?: ""
        return if (encryptedPassword.isNotEmpty()) {
            encryption.decrypt(encryptedPassword) // 비밀번호 복호화
        } else ""
    }

    fun clearPassword() {
        editor.apply {
            remove(KEY_PASSWORD)
            apply()
        }
    }

    fun getLoginSession(): HashMap<String, String?> {
        val userData = HashMap<String, String?>()
        userData[KEY_EMAIL] = sharedPreferences.getString(KEY_EMAIL, null)
        userData[KEY_UID] = sharedPreferences.getString(KEY_UID, null)
        return userData
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGIN, false)
    }

    fun clearSession() {
        editor.apply {
            clear()
            apply()
        }
    }
}

// 간단한 암호화 유틸리티 클래스
class EncryptionUtils {
    private val secretKey = "YourSecretKey" // 실제로는 더 안전하게

    fun encrypt(text: String): String {
        return android.util.Base64.encodeToString(text.toByteArray(), android.util.Base64.DEFAULT)
    }

    fun decrypt(text: String): String {
        return String(android.util.Base64.decode(text, android.util.Base64.DEFAULT))
    }
}