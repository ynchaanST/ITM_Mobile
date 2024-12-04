package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var recentActivityRestaurantName: TextView
    private lateinit var recentActivityDate: TextView
    private lateinit var recentActivityMenu: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // FirebaseAuth 초기화
        auth = FirebaseAuth.getInstance()

        // UI 요소 초기화
        emailTextView = view.findViewById(R.id.profile_email)
        nameTextView = view.findViewById(R.id.profile_name)
        profileImageView = view.findViewById(R.id.profile_image)
        settingsIcon = view.findViewById(R.id.settings_icon)
        recentActivityRestaurantName = view.findViewById(R.id.card1_restaurant_name)
        recentActivityDate = view.findViewById(R.id.card1_content)
        recentActivityMenu = view.findViewById(R.id.card1_menu)

        // 사용자 데이터 로드
        loadProfileData()

        // Recent Activity 데이터 로드
        loadRecentActivity()

        // Settings 버튼 클릭 리스너 설정
        setupSettingsButton()

        return view
    }

    override fun onResume() {
        super.onResume()
        // SettingsActivity에서 수정된 데이터를 반영하기 위해 데이터 다시 로드
        loadProfileData()
    }

    private fun loadProfileData() {
        val user = auth.currentUser

        if (user != null) {
            // 이메일 설정
            emailTextView.text = user.email ?: "No email available"

            // 이름 설정
            nameTextView.text = user.displayName ?: "Default Name"

            // Firebase Storage에서 프로필 사진 가져오기
            val storageRef =
                FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.default_profile) // 기본 프로필 이미지
                    .into(profileImageView)
            }.addOnFailureListener {
                profileImageView.setImageResource(R.drawable.default_profile) // 실패 시 기본 이미지
            }
        } else {
            // 사용자 인증이 없는 경우
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecentActivity() {
        val user = auth.currentUser
        if (user == null) return

        val db = FirebaseFirestore.getInstance()

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

                    // Recent Activity 데이터 업데이트
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
            Toast.makeText(
                requireContext(),
                "Unable to open settings. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}