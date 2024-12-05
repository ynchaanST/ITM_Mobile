package com.example.mychelin_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantHistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterSpinner: Spinner
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: RestaurantHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_restaurant_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.restaurant_recycler_view)
        filterSpinner = view.findViewById(R.id.filter_spinner)

        setupRecyclerView()
        setupSpinner()
        loadRestaurants(null) // 초기 로드는 모든 식당
    }

    private fun setupRecyclerView() {
        adapter = RestaurantHistoryAdapter { restaurant ->
            // 식당 상세 정보로 이동
            showRestaurantDetail(restaurant)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@RestaurantHistoryFragment.adapter
        }
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSpinner.adapter = adapter
        }

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val rating = when (pos) {
                    0 -> null // All restaurants
                    else -> pos // 1, 2, or 3 stars
                }
                loadRestaurants(rating)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadRestaurants(rating: Int?) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("visitData")
            .get()
            .addOnSuccessListener { documents ->
                val restaurants = documents.mapNotNull { doc ->
                    val restaurantRating = doc.getLong("rating")?.toInt() ?: 0
                    if (rating == null || restaurantRating == rating) {
                        RestaurantHistoryItem(
                            name = doc.getString("restaurantName") ?: "",
                            address = doc.getString("address") ?: "",
                            rating = restaurantRating,
                            lastVisited = doc.getString("lastVisitDate") ?: "",
                            totalSpent = doc.getDouble("totalSpent") ?: 0.0
                        )
                    } else null
                }
                adapter.submitList(restaurants)
            }
    }

    private fun showRestaurantDetail(restaurant: RestaurantHistoryItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(restaurant.name)
            .setMessage(
                """
            주소: ${restaurant.address}
            별점: ${restaurant.rating}
            마지막 방문: ${restaurant.lastVisited}
            총 지출: ${String.format("%,d", restaurant.totalSpent.toInt())}원
            """.trimIndent()
            )
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}