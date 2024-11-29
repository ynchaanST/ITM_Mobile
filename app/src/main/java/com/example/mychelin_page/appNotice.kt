package com.example.mychelin_page

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class appNotice : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("notice", "fcm todk: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("notice", "fcm message: ${message.data}")
    }
}

// Manifest, service 부분
// gradle, firebase messaging-kts dependency 부분
// 참고자료 : https://www.youtube.com/watch?v=p-3dpi60W0c