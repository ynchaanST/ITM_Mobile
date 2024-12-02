package com.example.mychelin_page

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore

class ReservationNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val reservationId = inputData.getString("reservationId") ?: return Result.failure()
        val restaurantName = inputData.getString("restaurantName") ?: return Result.failure()
        val userId = inputData.getString("userId") ?: return Result.failure()

        // 알림 생성
        createNotification(restaurantName)

        // Firestore에 알림 저장
        val notification = NotificationData(
            title = "예약 알림",
            message = "${restaurantName} 예약 시간이 1시간 남았습니다.",
            timestamp = System.currentTimeMillis(),
            type = "RESERVATION",
            reservationId = reservationId
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("notifications")
            .add(notification)

        return Result.success()
    }

    private fun createNotification(restaurantName: String) {
        val channelId = "reservation_notification"
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // 알림 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reservation Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("예약 알림")
            .setContentText("${restaurantName} 예약 시간이 1시간 남았습니다.")
            .setSmallIcon(R.drawable.icon_reservation)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}