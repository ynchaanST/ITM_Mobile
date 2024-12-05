package com.example.mychelin_page

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExpenseDetailFragment : Fragment() {
    private lateinit var barChart: BarChart
    private lateinit var filterSpinner: Spinner
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.expense_chart)
        filterSpinner = view.findViewById(R.id.filter_spinner)

        setupSpinner()
        setupChart()
        loadData("monthly") // 초기 로드는 월별 데이터
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.expense_filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSpinner.adapter = adapter
        }

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                when (pos) {
                    0 -> loadData("monthly")
                    1 -> loadData("daily")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(true)
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun loadData(filterType: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("visitData")
            .get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<BarEntry>()
                val labels = mutableListOf<String>()

                when (filterType) {
                    "monthly" -> processMonthlyData(documents.documents, entries, labels)
                    "daily" -> processDailyData(documents.documents, entries, labels)
                }

                updateChart(entries, labels)
            }
    }

    private fun processMonthlyData(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>,
        entries: MutableList<BarEntry>,
        labels: MutableList<String>
    ) {
        val monthlyTotals = mutableMapOf<String, Float>()

        documents.forEach { doc ->
            val date = doc.getString("lastVisitDate") ?: return@forEach
            val amount = doc.getDouble("amountSpent")?.toFloat() ?: 0f

            // Extract "YYYY-MM" from the date
            val month = date.substring(0, 7)
            monthlyTotals[month] = (monthlyTotals[month] ?: 0f) + amount
        }

        // Fill missing months with 0
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        for (i in 0 until 12) {
            calendar.set(currentYear, i, 1)
            val monthLabel = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            monthlyTotals[monthLabel] = monthlyTotals[monthLabel] ?: 0f
        }

        monthlyTotals.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value))
            labels.add(entry.key)
        }
    }

    private fun processDailyData(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>,
        entries: MutableList<BarEntry>,
        labels: MutableList<String>
    ) {
        val dailyTotals = mutableMapOf<String, Float>()
        val calendar = Calendar.getInstance()

        // Get current year and month
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Initialize all days in the current month to 0
        calendar.set(currentYear, currentMonth, 1)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDay) {
            val dayLabel = String.format("%d-%02d-%02d", currentYear, currentMonth + 1, day)
            dailyTotals[dayLabel] = 0f
        }

        documents.forEach { doc ->
            val date = doc.getString("lastVisitDate") ?: return@forEach
            val amount = doc.getDouble("amountSpent")?.toFloat() ?: 0f

            if (dailyTotals.containsKey(date)) {
                dailyTotals[date] = dailyTotals[date]!! + amount
            }
        }

        dailyTotals.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value))
            labels.add(entry.key.substring(8, 10)) // Extract only the day (e.g., "01", "02")
        }
    }

    private fun updateChart(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Expenses").apply {
            color = Color.rgb(139, 0, 0) // 진한 빨간색
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelRotationAngle = 45f
            xAxis.granularity = 1f // Ensure labels are displayed for each bar
            invalidate()
        }
    }
}
