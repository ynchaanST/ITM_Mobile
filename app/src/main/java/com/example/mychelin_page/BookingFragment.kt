package com.example.mychelin_page

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Calendar
import android.content.Context
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class BookingFragment : Fragment(R.layout.fragment_booking) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var workManager: WorkManager

    // UI 컴포넌트들
    private lateinit var restaurantImage: ImageView
    private lateinit var restaurantNameText: TextView
    private lateinit var restaurantRating: RatingBar
    private lateinit var restaurantAddressText: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var timePicker: TimePicker
    private lateinit var adultCount: TextView
    private lateinit var requestEditText: EditText
    private lateinit var bookingButton: Button
    private lateinit var decreaseAdults: ImageButton
    private lateinit var increaseAdults: ImageButton

    private var currentAdults = 2
    private lateinit var selectedDate: Calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workManager = WorkManager.getInstance(requireContext())
        initViews(view)
        setupHeadcountListeners()
        setupBookingButtonListener()

        // 레스토랑 정보 로드
        val restaurantId = arguments?.getString("restaurantId")
        val tableNumber = arguments?.getInt("tableNumber") ?: 0

        if (restaurantId != null) {
            loadRestaurantInfo(restaurantId)
        }

        // 캘린더 설정 수정
        setupCalendar()
    }

    private fun setupCalendar() {
        val today = Calendar.getInstance() // 현재 시간을 그대로 사용
        selectedDate = Calendar.getInstance() // 초기값은 오늘 날짜

        val maxDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1) // 1개월 후까지 예약 가능
        }

        calendarView.minDate = today.timeInMillis
        calendarView.maxDate = maxDate.timeInMillis
        calendarView.date = today.timeInMillis

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth) // 선택한 날짜 업데이트
        }
    }


    private fun initViews(view: View) {
        restaurantImage = view.findViewById(R.id.restaurantImage)
        restaurantNameText = view.findViewById(R.id.restaurantNameText)
        restaurantRating = view.findViewById(R.id.restaurantRating)
        restaurantAddressText = view.findViewById(R.id.restaurantAddressText)
        calendarView = view.findViewById(R.id.calendarView)
        timePicker = view.findViewById(R.id.timePicker)
        decreaseAdults = view.findViewById(R.id.decreaseAdults)
        increaseAdults = view.findViewById(R.id.increaseAdults)
        adultCount = view.findViewById(R.id.adultCount)
        requestEditText = view.findViewById(R.id.requestEditText)
        bookingButton = view.findViewById(R.id.bookingButton)
    }

    private fun loadRestaurantInfo(restaurantId: String) {
        db.collection("restaurants")
            .document(restaurantId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: ""
                    val address = document.getString("road_address") ?: ""
                    val rating = document.getDouble("rating") ?: 0.0

                    restaurantNameText.text = name
                    restaurantAddressText.text = address
                    restaurantRating.rating = rating.toFloat()

                    // 이미지는 기본 이미지 사용
                    restaurantImage.setImageResource(R.drawable.ic_restaurant)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "레스토랑 정보 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupHeadcountListeners() {
        decreaseAdults.setOnClickListener {
            if (currentAdults > 1) {
                currentAdults--
                adultCount.text = currentAdults.toString()
            }
        }

        increaseAdults.setOnClickListener {
            if (currentAdults < 10) {
                currentAdults++
                adultCount.text = currentAdults.toString()
            }
        }
    }

    private fun setupBookingButtonListener() {
        bookingButton.setOnClickListener {
            val restaurantId = arguments?.getString("restaurantId")
            val tableNumber = arguments?.getInt("tableNumber")

            if (restaurantId == null || tableNumber == null) {
                Toast.makeText(context, "예약 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentTime = Calendar.getInstance() // 현재 시간
            val selectedTime = selectedDate.apply {
                set(Calendar.HOUR_OF_DAY, timePicker.hour)
                set(Calendar.MINUTE, timePicker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // 영업 시간 체크
            if (timePicker.hour < 10 || timePicker.hour >= 22) {
                Toast.makeText(context, "예약 가능 시간은 오전 10시부터 오후 10시까지입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 선택된 시간이 현재 시간보다 이전인지 확인
            if (isSameDate(selectedTime, currentTime)) {
                // 같은 날이라면 시간 비교
                if (selectedTime.timeInMillis < currentTime.timeInMillis) {
                    Toast.makeText(context, "현재 시간 이후로 예약해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // 예약 생성
            val bundle = Bundle().apply {
                putString("restaurantId", restaurantId)
                putLong("selectedDate", selectedDate.timeInMillis)
                putInt("selectedHour", timePicker.hour)
                putInt("selectedMinute", timePicker.minute)
                putInt("numberOfGuests", currentAdults)
                putString("specialRequests", requestEditText.text.toString())
            }

            findNavController().navigate(R.id.action_booking_to_restaurant_table, bundle)
        }
    }


    private fun isSameDate(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
    // 나머지 메서드들은 동일...
    private fun createReservation(
        restaurantId: String,
        tableNumber: Int,
        reservationDate: Long,
        numberOfGuests: Int,
        specialRequests: String
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore에 예약 정보 저장
        db.collection("restaurants").document(restaurantId).get()
            .addOnSuccessListener { restaurantDoc ->
                val restaurantName = restaurantDoc.getString("name") ?: ""
                val restaurantAddress = restaurantDoc.getString("road_address") ?: ""

                val reservation = hashMapOf(
                    "restaurantId" to restaurantId,
                    "restaurantName" to restaurantName,
                    "restaurantAddress" to restaurantAddress,
                    "userId" to userId,
                    "tableNumber" to tableNumber,
                    "reservationDate" to reservationDate,
                    "numberOfGuests" to numberOfGuests,
                    "specialRequests" to specialRequests,
                    "status" to "CONFIRMED",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("users")
                    .document(userId)
                    .collection("reservationData")
                    .add(reservation)
                    .addOnSuccessListener { documentReference ->
                        updateTableStatus(restaurantId, tableNumber, false)
                        scheduleReservationNotification(
                            documentReference.id,
                            restaurantName,
                            reservationDate
                        )

                        Toast.makeText(context, "예약이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.menu_reservation)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "예약 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun updateTableStatus(restaurantId: String, tableNumber: Int, isAvailable: Boolean) {
        db.collection("restaurants")
            .document(restaurantId)
            .collection("tables")
            .whereEqualTo("tableNumber", tableNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val tableDoc = documents.documents[0]
                    tableDoc.reference.update("isAvailable", isAvailable)
                }
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