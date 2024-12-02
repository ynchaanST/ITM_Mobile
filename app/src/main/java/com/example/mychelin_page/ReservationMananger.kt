package com.example.mychelin_page

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class ReservationManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val workManager = WorkManager.getInstance(context)

    fun createReservation(
        restaurantId: String,
        restaurantName: String,
        tableNumber: Int,
        reservationDate: Long,
        numberOfGuests: Int,
        specialRequests: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        val reservation = ReservationData(
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            userId = userId,
            tableNumber = tableNumber,
            reservationDate = reservationDate,
            numberOfGuests = numberOfGuests,
            specialRequests = specialRequests
        )

        // Firestore에 예약 데이터 저장
        db.collection("users")
            .document(userId)
            .collection("reservationData")
            .add(reservation)
            .addOnSuccessListener { documentReference ->
                // 알림 스케줄링
                scheduleReservationNotification(
                    documentReference.id,
                    restaurantName,
                    reservationDate
                )
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    private fun scheduleReservationNotification(
        reservationId: String,
        restaurantName: String,
        reservationDate: Long
    ) {
        val notificationWork = OneTimeWorkRequestBuilder<ReservationNotificationWorker>()
            .setInitialDelay(
                reservationDate - System.currentTimeMillis() - 3600000, // 1시간 전
                TimeUnit.MILLISECONDS
            )
            .setInputData(
                workDataOf(
                    "reservationId" to reservationId,
                    "restaurantName" to restaurantName
                )
            )
            .build()

        workManager.enqueue(notificationWork)
    }
}