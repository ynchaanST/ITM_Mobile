package com.example.mychelin_page

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RestaurantTableFragment : Fragment(R.layout.fragment_restaurant_table) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var restaurantName: String = ""

    private var selectedDate: Long = 0
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private var numberOfGuests: Int = 0
    private var specialRequests: String = ""
    private var restaurantId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            restaurantId = it.getString("restaurantId", "")
            selectedDate = it.getLong("selectedDate", 0)
            selectedHour = it.getInt("selectedHour", 0)
            selectedMinute = it.getInt("selectedMinute", 0)
            numberOfGuests = it.getInt("numberOfGuests", 0)
            specialRequests = it.getString("specialRequests", "")
        }

        updateReservationInfo()
        loadAvailableTables()
    }

    private fun loadAvailableTables() {
        val selectedDateTime = Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }

        db.collection("restaurants")
            .document(restaurantId)
            .get()
            .addOnSuccessListener { document ->
                restaurantName = document.getString("name") ?: ""

                db.collection("restaurants")
                    .document(restaurantId)
                    .collection("reservations")
                    .get()
                    .addOnSuccessListener { reservations ->
                        val reservedTables = mutableMapOf<Int, List<Pair<Long, Long>>>()

                        reservations.forEach { doc ->
                            val tableNo = doc.getLong("tableNumber")?.toInt() ?: 0
                            val reservationTime = doc.getLong("reservationDate") ?: 0L
                            val reservationEndTime = reservationTime + (2 * 60 * 60 * 1000)

                            reservedTables[tableNo] = reservedTables.getOrDefault(tableNo, listOf()) +
                                    Pair(reservationTime, reservationEndTime)
                        }

                        setupTableViews(selectedDateTime.timeInMillis, reservedTables)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "테이블 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "레스토랑 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupTableViews(selectedTime: Long, reservedTables: Map<Int, List<Pair<Long, Long>>>) {
        val tableLayout = view?.findViewById<GridLayout>(R.id.tableLayout)
        tableLayout?.removeAllViews()

        for (i in 1..9) {
            val tableNumber = i
            val reservations = reservedTables[tableNumber]

            val isAvailable = reservations?.all { (start, end) ->
                selectedTime >= end || (selectedTime + (2 * 60 * 60 * 1000)) <= start
            } ?: true

            val tableContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
            }

            val tableIcon = ImageView(context).apply {
                setImageResource(R.drawable.ic_table)
                layoutParams = LinearLayout.LayoutParams(120, 120)
                if (!isAvailable) {
                    setColorFilter(Color.GRAY)
                }
            }

            val tableText = TextView(context).apply {
                text = "테이블 $tableNumber"
                setTextColor(if (isAvailable) Color.WHITE else Color.GRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            tableContainer.addView(tableIcon)
            tableContainer.addView(tableText)

            if (isAvailable) {
                tableContainer.setOnClickListener {
                    showConfirmationDialog(tableNumber)
                }
            }

            tableLayout?.addView(tableContainer)
        }
    }

    private fun showConfirmationDialog(tableNumber: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("예약 확인")
            .setMessage("테이블 ${tableNumber}번을 예약하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                createReservation(tableNumber)
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    private fun createReservation(tableNumber: Int) {
        val userId = auth.currentUser?.uid ?: return

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }

        val reservationStartTime = calendar.timeInMillis
        val reservationEndTime = reservationStartTime + (2 * 60 * 60 * 1000)

        db.collection("restaurants")
            .document(restaurantId)
            .collection("reservations")
            .whereEqualTo("tableNumber", tableNumber)
            .get()
            .addOnSuccessListener { documents ->
                val isConflict = documents.any { doc ->
                    val start = doc.getLong("reservationDate") ?: 0L
                    val end = start + (2 * 60 * 60 * 1000)
                    !(reservationEndTime <= start || reservationStartTime >= end)
                }

                if (isConflict) {
                    Toast.makeText(context, "이미 예약된 시간대입니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val reservation = hashMapOf(
                    "restaurantId" to restaurantId,
                    "restaurantName" to restaurantName,
                    "userId" to userId,
                    "tableNumber" to tableNumber,
                    "reservationDate" to reservationStartTime,
                    "numberOfGuests" to numberOfGuests,
                    "specialRequests" to specialRequests,
                    "status" to "CONFIRMED",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("restaurants")
                    .document(restaurantId)
                    .collection("reservations")
                    .add(reservation)
                    .addOnSuccessListener { documentReference ->
                        val reservationId = documentReference.id

                        db.collection("users")
                            .document(userId)
                            .collection("reservationData")
                            .document(reservationId)
                            .set(reservation)
                            .addOnSuccessListener {
                                createReservationNotification(userId, reservationId, reservationStartTime)
                                Toast.makeText(context, "예약이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(
                                    R.id.action_restaurantTable_to_reservation,
                                    null, // bundle이 필요없다면 null로 설정
                                    navOptions {
                                        popUpTo(R.id.page_booking) { // booking 페이지까지 백스택에서 제거
                                            inclusive = true
                                        }
                                        // 추가로 현재 테이블 선택 페이지도 제거
                                        popUpTo(R.id.page_restaurant_table) {
                                            inclusive = true
                                        }
                                    }
                                )
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "예약 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "예약 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "예약 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createReservationNotification(userId: String, reservationId: String, reservationStartTime: Long) {
        val notification = NotificationData(
            title = "예약 확정",
            message = "${restaurantName} ${selectedHour}시 ${selectedMinute}분 예약이 확정되었습니다.",
            timestamp = System.currentTimeMillis(),
            type = "RESERVATION",
            reservationId = reservationId
        )

        db.collection("users")
            .document(userId)
            .collection("notifications")
            .add(notification)

        val notificationWork = OneTimeWorkRequestBuilder<ReservationNotificationWorker>()
            .setInitialDelay(
                reservationStartTime - System.currentTimeMillis() - 3600000,
                TimeUnit.MILLISECONDS
            )
            .setInputData(
                workDataOf(
                    "reservationId" to reservationId,
                    "restaurantName" to restaurantName,
                    "userId" to userId
                )
            )
            .build()

        WorkManager.getInstance(requireContext()).enqueue(notificationWork)
    }

    private fun updateReservationInfo() {
        view?.let {
            val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.getDefault())
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedDate
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }

            it.findViewById<TextView>(R.id.selectedDateTimeText)?.text =
                "예약 시간: ${dateFormat.format(calendar.time)}"
            it.findViewById<TextView>(R.id.selectedGuestsText)?.text =
                "예약 인원: ${numberOfGuests}명"
        }
    }
}