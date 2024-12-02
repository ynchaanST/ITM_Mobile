package com.example.mychelin_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservationAdapter(
    private val onItemClicked: (ReservationData) -> Unit
) : ListAdapter<ReservationData, ReservationAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(itemView: View, private val onItemClicked: (ReservationData) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val restaurantNameText: TextView = itemView.findViewById(R.id.restaurantNameText)
        private val dateTimeText: TextView = itemView.findViewById(R.id.reservationDateTimeText)
        private val guestsText: TextView = itemView.findViewById(R.id.guestsText)
//        private val statusText: TextView = itemView.findViewById(R.id.statusText)

        fun bind(reservation: ReservationData) {
            restaurantNameText.text = reservation.restaurantName
            dateTimeText.text = formatDateTime(reservation.reservationDate)
            guestsText.text = "인원: ${reservation.numberOfGuests}명"
//            statusText.text = "상태: ${reservation.status}"

            itemView.setOnClickListener {
                onItemClicked(reservation)
            }
        }

        private fun formatDateTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(itemView, onItemClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<ReservationData>() {
        override fun areItemsTheSame(oldItem: ReservationData, newItem: ReservationData) =
            oldItem.reservationDate == newItem.reservationDate

        override fun areContentsTheSame(oldItem: ReservationData, newItem: ReservationData) =
            oldItem == newItem
    }
}
