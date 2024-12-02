package com.example.mychelin_page

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TableAdapter(
    private val tables: List<TableInfo>,
    private val onTableSelected: (TableInfo) -> Unit
) : RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    class TableViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tableIcon: ImageView = view.findViewById(R.id.table_icon)
        private val tableNumber: TextView = view.findViewById(R.id.table_number)
        private val tableCapacity: TextView = view.findViewById(R.id.table_capacity)
        private val tableStatus: TextView = view.findViewById(R.id.table_status)

        fun bind(table: TableInfo, onTableSelected: (TableInfo) -> Unit) {
            tableNumber.text = "테이블 ${table.tableNumber}"
            tableCapacity.text = "${table.capacity}인석"

            if (table.isAvailable) {
                tableStatus.text = "예약 가능"
                tableStatus.setTextColor(Color.parseColor("#4CAF50")) // 초록색
                itemView.setOnClickListener { onTableSelected(table) }
            } else {
                tableStatus.text = "예약됨"
                tableStatus.setTextColor(Color.parseColor("#FF5722")) // 빨간색
                tableIcon.alpha = 0.5f // 불투명도 설정으로 예약된 테이블 표시
            }

            // 테이블 아이콘 색상 설정
            tableIcon.setColorFilter(
                if (table.isAvailable) Color.parseColor("#4CAF50")
                else Color.parseColor("#FF5722"),
                PorterDuff.Mode.SRC_IN
            )

            // 클릭 효과 설정
            itemView.isClickable = table.isAvailable
            itemView.background = if (table.isAvailable) {
                ContextCompat.getDrawable(itemView.context, R.drawable.ripple_effect)
            } else {
                null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_table, parent, false)
        return TableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(tables[position], onTableSelected)
    }

    override fun getItemCount() = tables.size
}