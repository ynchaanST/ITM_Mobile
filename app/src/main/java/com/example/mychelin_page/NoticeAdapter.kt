package com.example.mychelin_page

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoticeAdapter(
    private val onItemClick: (NotificationData) -> Unit,
    private val onItemDismiss: (NotificationData) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {
    private val notifications = mutableListOf<NotificationData>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleText: TextView = view.findViewById(R.id.notification_title)
        private val messageText: TextView = view.findViewById(R.id.notification_message)
        private val timeText: TextView = view.findViewById(R.id.notification_time)
        private val container: View = view.findViewById(R.id.notification_container)

        fun bind(notification: NotificationData) {
            titleText.text = notification.title
            messageText.text = notification.message
            timeText.text = formatTime(notification.timestamp)

            if (!notification.isRead) {
                container.setBackgroundResource(R.color.white)
            } else {
                container.setBackgroundResource(R.color.silver)
            }

            itemView.setOnClickListener {
                onItemClick(notification)
            }

            // 스와이프 기능을 위한 터치 이벤트 처리
            var startX = 0f
            itemView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val endX = event.x
                        val deltaX = endX - startX
                        if (deltaX < -100) { // 왼쪽으로 스와이프
                            onItemDismiss(notification)
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    // 알림 리스트 업데이트
    fun updateNotifications(newNotifications: List<NotificationData>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    // 특정 알림 제거
    fun removeNotification(notification: NotificationData) {
        val position = notifications.indexOf(notification)
        if (position != -1) {
            notifications.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}