package com.example.mychelin_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoticeAdapter(
    private val onItemClick: (NotificationData) -> Unit
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

            container.setBackgroundResource(
                if (!notification.isRead) R.color.white else R.color.silver
            )

            itemView.setOnClickListener {
                onItemClick(notification)
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

    fun getNotificationAt(position: Int): NotificationData {
        return notifications[position]
    }

    fun updateNotifications(newNotifications: List<NotificationData>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }
}