package com.example.mychelin_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RestaurantHistoryAdapter(
    private val onItemClick: (RestaurantHistoryItem) -> Unit
) : ListAdapter<RestaurantHistoryItem, RestaurantHistoryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        private val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        private val lastVisitDate: TextView = itemView.findViewById(R.id.lastVisitDate)
        private val totalSpent: TextView = itemView.findViewById(R.id.totalSpent)
        private val star1: ImageView = itemView.findViewById(R.id.star1)
        private val star2: ImageView = itemView.findViewById(R.id.star2)
        private val star3: ImageView = itemView.findViewById(R.id.star3)

        fun bind(item: RestaurantHistoryItem) {
            restaurantName.text = item.name
            restaurantAddress.text = item.address
            lastVisitDate.text = "마지막 방문: ${item.lastVisited}"
            totalSpent.text = "총 지출: ${String.format("%,d", item.totalSpent.toInt())}원"

            // 별점 표시 (ic_star_wine 사용)
            val stars = listOf(star1, star2, star3)
            stars.forEachIndexed { index, star ->
                star.visibility = if (index < item.rating) View.VISIBLE else View.GONE
                if (index < item.rating) {
                    star.setImageResource(R.drawable.ic_star_wine)
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<RestaurantHistoryItem>() {
        override fun areItemsTheSame(oldItem: RestaurantHistoryItem, newItem: RestaurantHistoryItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RestaurantHistoryItem, newItem: RestaurantHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}