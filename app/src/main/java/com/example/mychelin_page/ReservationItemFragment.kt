package com.example.mychelin_page

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReservationItemFragment : Fragment(R.layout.fragment_reservation_item) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var restaurantImage: ImageView
    private lateinit var restaurantNameText: TextView
    private lateinit var restaurantAddressText: TextView
    private lateinit var restaurantPhoneText: TextView
    private lateinit var bookingDateText: TextView
    private lateinit var bookingTimeText: TextView
    private lateinit var guestCountText: TextView
    private lateinit var requestEditText: EditText
//    private lateinit var reviseButton: Button
    private lateinit var cancelButton: Button
//    private lateinit var confirmButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reservation_item, container, false)
        initializeViews(view)
        setupListeners()
        loadReservationData()
        return view
    }

    private fun initializeViews(view: View) {
        restaurantImage = view.findViewById(R.id.restaurantImage)
        restaurantNameText = view.findViewById(R.id.restaurantNameText)
        restaurantAddressText = view.findViewById(R.id.restaurantAddressText)
        restaurantPhoneText = view.findViewById(R.id.restaurantPhoneText)
        bookingDateText = view.findViewById(R.id.bookingDateText)
        bookingTimeText = view.findViewById(R.id.bookingTimeText)
        guestCountText = view.findViewById(R.id.guestCountText)
        requestEditText = view.findViewById(R.id.requestEditText)
//        reviseButton = view.findViewById(R.id.resReviseButton)
        cancelButton = view.findViewById(R.id.resCancleButton)
//        confirmButton = view.findViewById(R.id.resConfirmButton)

        // Confirm 버튼과 Revise 버튼 숨기기
//        confirmButton.visibility = View.GONE
//        reviseButton.visibility = View.GONE
        cancelButton.visibility = View.GONE
    }

    private fun setupListeners() {
        val reservationId = arguments?.getString("reservationId")

        cancelButton.setOnClickListener {
            if (reservationId != null) {
                showCancelConfirmationDialog(reservationId)
            }
        }
    }

    private fun showCancelConfirmationDialog(reservationId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("예약 취소")
            .setMessage("예약을 취소하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                cancelReservation(reservationId)
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    private fun cancelReservation(reservationId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("reservationData")
            .document(reservationId)
            .get()
            .addOnSuccessListener { document ->
                val restaurantId = document.getString("restaurantId")
                if (restaurantId != null) {
                    // 레스토랑 컬렉션에서 예약 삭제
                    db.collection("restaurants")
                        .document(restaurantId)
                        .collection("reservations")
                        .document(reservationId)
                        .delete()
                        .addOnSuccessListener {
                            // 유저 컬렉션에서 예약 삭제
                            db.collection("users")
                                .document(userId)
                                .collection("reservationData")
                                .document(reservationId)
                                .delete()
                                .addOnSuccessListener {
                                    // 예약 취소 알림 생성
                                    createCancellationNotification(restaurantId)
                                    Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
//                                    findNavController().navigate(R.id.action_reservationItem_to_reservation)
                                }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "예약 취소 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createCancellationNotification(restaurantId: String) {
        val userId = auth.currentUser?.uid ?: return
        val notification = NotificationData(
            title = "예약 취소 완료",
            message = "${restaurantNameText.text} 예약이 취소되었습니다.",
            timestamp = System.currentTimeMillis(),
            type = "CANCELLATION"
        )

        db.collection("users")
            .document(userId)
            .collection("notifications")
            .add(notification)
    }

    private fun loadReservationData() {
        val reservationId = arguments?.getString("reservationId")
        if (reservationId.isNullOrEmpty()) {
            Toast.makeText(context, "예약 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("reservationData")
            .document(reservationId)
            .get()
            .addOnSuccessListener { document ->
                val reservation = document.toObject(ReservationData::class.java)
                if (reservation != null) {
                    updateUI(reservation)
                    cancelButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "예약 데이터를 로드할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(reservation: ReservationData) {
        restaurantNameText.text = reservation.restaurantName
        bookingDateText.text = "일시: ${formatDate(reservation.reservationDate)}"
        bookingTimeText.text = "시간: ${formatTime(reservation.reservationDate)}"
        guestCountText.text = "인원: 성인 ${reservation.numberOfGuests}명"
        requestEditText.setText(reservation.specialRequests ?: "")
        restaurantImage.setImageResource(R.drawable.ic_restaurant)
        requestEditText.isEnabled = false  // 수정 불가능하도록 설정
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH시 mm분", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}