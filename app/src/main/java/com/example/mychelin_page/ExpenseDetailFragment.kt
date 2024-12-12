package com.example.mychelin_page

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExpenseDetailFragment : Fragment() {
    private lateinit var lineChart: LineChart
    private lateinit var viewModeSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense_detail, container, false)

        lineChart = view.findViewById(R.id.lineChart)
        viewModeSpinner = view.findViewById(R.id.viewModeSpinner)

        setupSpinner()
        setupChart()

        return view
    }

    private fun setupSpinner() {
        val modes = arrayOf("월별", "일별")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, modes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        viewModeSpinner.adapter = adapter
        viewModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> fetchExpenseData(true)  // 월별
                    1 -> fetchExpenseData(false) // 일별
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f  // Y축 최소값을 0으로 설정
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun fetchExpenseData(isMonthly: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val startDate = if (isMonthly) {
            Calendar.getInstance().apply {
                set(Calendar.MONTH, 0)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.time
        } else {
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.time
        }

        db.collection("users")
            .document(userId)
            .collection("visitData")
            .get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<Entry>()
                val xAxisLabels = mutableListOf<String>()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                if (isMonthly) {
                    // 월별 데이터 처리
                    val monthlyData = documents.mapNotNull { doc ->
                        val amountSpent = doc.getDouble("amountSpent") ?: return@mapNotNull null
                        val lastVisitDateStr = doc.getString("lastVisitDate") ?: return@mapNotNull null
                        val lastVisitDate = dateFormat.parse(lastVisitDateStr) ?: return@mapNotNull null
                        Pair(lastVisitDate, amountSpent)
                    }.groupBy { pair ->
                        Calendar.getInstance().apply { time = pair.first }.get(Calendar.MONTH)
                    }.mapValues { it.value.sumOf { pair -> pair.second } }

                    for (month in 0..11) {
                        entries.add(Entry(month.toFloat(), monthlyData[month]?.toFloat() ?: 0f))
                        xAxisLabels.add("${month + 1}월")
                    }
                } else {
                    // 일별 데이터 처리
                    val dailyData = documents.mapNotNull { doc ->
                        val amountSpent = doc.getDouble("amountSpent") ?: return@mapNotNull null
                        val lastVisitDateStr = doc.getString("lastVisitDate") ?: return@mapNotNull null
                        val lastVisitDate = dateFormat.parse(lastVisitDateStr) ?: return@mapNotNull null
                        Pair(lastVisitDate, amountSpent)
                    }.groupBy { pair ->
                        Calendar.getInstance().apply { time = pair.first }.get(Calendar.DAY_OF_MONTH)
                    }.mapValues { it.value.sumOf { pair -> pair.second } }

                    val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                    for (day in 1..daysInMonth) {
                        val value = dailyData[day]?.toFloat() ?: 0f
                        entries.add(Entry((day - 1).toFloat(), value)) // 0원 데이터도 Entry 생성
                        xAxisLabels.add("${day}일")
                    }
                }

                updateChart(entries.filter { it.y > 0 }, xAxisLabels)
            }
            .addOnFailureListener { e ->
                Log.e("ExpenseDetailFragment", "Error fetching data", e)
            }
    }


    private fun updateChart(entries: List<Entry>, xAxisLabels: List<String>) {
        val dataSet = LineDataSet(entries, "지출액").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            valueTextSize = 10f
            lineWidth = 2f
            circleRadius = 4f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%,d원", value.toLong())
                }
            }
        }

        lineChart.apply {
            data = LineData(dataSet)

            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return xAxisLabels.getOrNull(value.toInt()) ?: ""
                }
            }

            notifyDataSetChanged()
            invalidate()
        }
    }
}