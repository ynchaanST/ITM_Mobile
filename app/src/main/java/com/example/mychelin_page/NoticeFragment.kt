package com.example.mychelin_page

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        setupSwipeToDelete()
        loadNotifications()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.notifications_recycler_view)
        adapter = NoticeAdapter { notification ->
            markAsRead(notification)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT // 왼쪽으로 스와이프만 허용
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notification = adapter.getNotificationAt(position)
                deleteNotification(notification)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
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
            .addOnSuccessListener {
                Toast.makeText(context, "알림이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "알림 삭제에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                // 삭제 실패 시 리스트 새로고침
                adapter.notifyDataSetChanged()
            }
    }
}