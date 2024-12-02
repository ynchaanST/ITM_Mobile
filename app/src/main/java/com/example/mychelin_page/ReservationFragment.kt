package com.example.mychelin_page

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReservationFragment : Fragment(R.layout.fragment_reservation) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var reservationAdapter: ReservationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        loadReservations()
    }

    private fun setupRecyclerView(view: View) {
        reservationAdapter = ReservationAdapter { reservationData ->
            val bundle = Bundle().apply {
                putString("reservationId", reservationData.reservationId) // reservationId를 Bundle에 추가
            }
            findNavController().navigate(R.id.action_reservation_to_reservationItem, bundle)
        }
        view.findViewById<RecyclerView>(R.id.reservationRecyclerView).apply {
            adapter = reservationAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadReservations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("reservationData")
            .orderBy("reservationDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reservations = documents.mapNotNull { doc ->
                    doc.toObject(ReservationData::class.java).apply {
                        // Firestore 문서 ID 저장 (필요 시)
                        reservationId = doc.id
                    }
                }
                reservationAdapter.submitList(reservations)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
