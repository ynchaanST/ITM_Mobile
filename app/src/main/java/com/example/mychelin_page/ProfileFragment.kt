package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var recentActivityRestaurantName: TextView
    private lateinit var recentActivityDate: TextView
    private lateinit var recentActivityMenu: TextView
    private lateinit var highestSpentRestaurantName: TextView
    private lateinit var highestSpentAmount: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // UI 요소 초기화
        emailTextView = view.findViewById(R.id.profile_email)
        nameTextView = view.findViewById(R.id.profile_name)
        profileImageView = view.findViewById(R.id.profile_image)
        settingsIcon = view.findViewById(R.id.settings_icon)
        recentActivityRestaurantName = view.findViewById(R.id.card1_restaurant_name)
        recentActivityDate = view.findViewById(R.id.card1_content)
        recentActivityMenu = view.findViewById(R.id.card1_menu)
        highestSpentRestaurantName = view.findViewById(R.id.highest_spent_restaurant_name)
        highestSpentAmount = view.findViewById(R.id.highest_spent_total)

        // 데이터 로드
        loadProfileData()
        loadRecentActivity()
        loadHighestSpentRestaurant()

        // 설정 버튼 동작 설정
        setupSettingsButton()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Resume 시 최신 데이터 다시 로드
        loadProfileData()
    }

    private fun loadProfileData() {
        val user = auth.currentUser

        if (user != null) {
            // Firebase Auth 데이터 설정
            emailTextView.text = user.email ?: "No email available"
            nameTextView.text = user.displayName ?: "Default Name"

            // Firebase Firestore에서 최신 데이터 가져오기
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name") ?: "Default Name"
                        val email = document.getString("email") ?: "No email available"
                        nameTextView.text = name
                        emailTextView.text = email
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
                }

            // Firebase Storage에서 프로필 사진 가져오기
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.default_profile)
                    .into(profileImageView)
            }.addOnFailureListener {
                profileImageView.setImageResource(R.drawable.default_profile)
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecentActivity() {
        val user = auth.currentUser
        if (user == null) return

        db.collection("users").document(user.uid).collection("visitData")
            .orderBy("lastVisitDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val restaurantName = document.getString("restaurantName") ?: "No data"
                    val lastVisitDate = document.getString("lastVisitDate") ?: "Unknown date"
                    val menu = document.getString("menu") ?: "No menu data"

                    recentActivityRestaurantName.text = restaurantName
                    recentActivityDate.text = "Last visited: $lastVisitDate"
                    recentActivityMenu.text = "Menu: $menu"
                } else {
                    recentActivityRestaurantName.text = "No recent activity"
                    recentActivityDate.text = "-"
                    recentActivityMenu.text = "-"
                }
            }
            .addOnFailureListener {
                recentActivityRestaurantName.text = "Failed to load"
                recentActivityDate.text = "-"
                recentActivityMenu.text = "-"
            }
    }

    private fun loadHighestSpentRestaurant() {
        val user = auth.currentUser
        if (user == null) return

        db.collection("users").document(user.uid).collection("visitData")
            .orderBy("totalSpent", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val restaurantName = document.getString("restaurantName") ?: "No data"
                    val totalSpent = document.getLong("totalSpent") ?: 0L

                    highestSpentRestaurantName.text = restaurantName
                    highestSpentAmount.text = "Total spent: $totalSpent"
                } else {
                    highestSpentRestaurantName.text = "No data"
                    highestSpentAmount.text = "-"
                }
            }
            .addOnFailureListener {
                highestSpentRestaurantName.text = "Failed to load"
                highestSpentAmount.text = "-"
            }
    }

    private fun setupSettingsButton() {
        settingsIcon.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun navigateToSettings() {
        try {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open settings. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
