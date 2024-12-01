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

class BookingFragment : Fragment(R.layout.fragment_booking) {
    private lateinit var restaurantImage: ImageView
    private lateinit var restaurantNameText: TextView
    private lateinit var restaurantRating: RatingBar
    private lateinit var restaurantAddressText: TextView

    private lateinit var calendarView: CalendarView
    private lateinit var timePicker: TimePicker

    private lateinit var decreaseAdults: ImageButton
    private lateinit var increaseAdults: ImageButton
    private lateinit var adultCount: TextView

    private lateinit var requestEditText: EditText
    private lateinit var bookingButton: Button

    private var currentAdults = 2
    private var selectedDate: Long = 0
    private var selectedTime: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View 초기화
        initViews(view)

        // 인원 조절 버튼 리스너 설정
        setupHeadcountListeners()

        // 예약 버튼 리스너 설정
        setupBookingButtonListener()

        // 기본 데이터 설정
        setDefaultData()
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
            // 예약 가능 여부 체크 및 예약 로직
            val selectedDate = calendarView.date
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val specialRequest = requestEditText.text.toString()

            // 유효성 검사 및 예약 처리
            if (validateBooking(selectedDate, selectedHour, selectedMinute)) {
                performBooking(selectedDate, selectedHour, selectedMinute, currentAdults, specialRequest)
            }
        }
    }

    private fun validateBooking(date: Long, hour: Int, minute: Int): Boolean {
        val currentTime = Calendar.getInstance()
        val selectedTime = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        // 현재 시간 이후인지 확인
        return if (selectedTime.after(currentTime)) {
            true
        } else {
            Toast.makeText(requireContext(), "유효하지 않은 날짜와 시간입니다.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun performBooking(date: Long, hour: Int, minute: Int, guests: Int, specialRequest: String) {
        // 실제 예약 처리 로직 (서버 통신, 데이터베이스 저장 등)
        Toast.makeText(
            requireContext(),
            "예약이 완료되었습니다.\n" +
                    "날짜: ${formatDate(date)}\n" +
                    "시간: $hour:$minute\n" +
                    "인원: $guests 명\n" +
                    "요청사항: $specialRequest",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun formatDate(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return "${calendar.get(Calendar.YEAR)}년 " +
                "${calendar.get(Calendar.MONTH) + 1}월 " +
                "${calendar.get(Calendar.DAY_OF_MONTH)}일"
    }

    private fun setDefaultData() {
        // 식당 정보 기본값 설정
        restaurantNameText.text = "음식점 기본값"
        restaurantRating.rating = 4.5f
        restaurantAddressText.text = "음식점 주소 기본값"

        // 캘린더 초기 설정
        calendarView.minDate = Calendar.getInstance().timeInMillis
        calendarView.maxDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, 3)
        }.timeInMillis
    }
}