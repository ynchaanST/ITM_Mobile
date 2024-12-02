package com.example.mychelin_page

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mychelin_page.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NoticeFragment : Fragment(R.layout.fragment_notice) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoticeAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        loadNotifications()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.notifications_recycler_view)
        adapter = NoticeAdapter(
            onItemClick = { notification ->
                markAsRead(notification)
            },
            onItemDismiss = { notification ->
                deleteNotification(notification)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(context, "알림을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val notifications = snapshots?.documents?.map { doc ->
                    doc.toObject(NotificationData::class.java)?.copy(id = doc.id)
                }?.filterNotNull() ?: listOf()

                adapter.updateNotifications(notifications)
            }
    }

    private fun markAsRead(notification: NotificationData) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("notifications")
            .document(notification.id)
            .update("isRead", true)
    }

    private fun deleteNotification(notification: NotificationData) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("notifications")
            .document(notification.id)
            .delete()
    }
}