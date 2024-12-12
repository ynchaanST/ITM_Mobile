package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    private lateinit var logoutIcon: ImageView
    private lateinit var recentActivityRestaurantName: TextView
    private lateinit var recentActivityDate: TextView
    private lateinit var recentActivityMenu: TextView
    private lateinit var sessionManager: SessionManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // FirebaseAuth 초기화
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(requireContext())

        // UI 요소 초기화
        emailTextView = view.findViewById(R.id.profile_email)
        nameTextView = view.findViewById(R.id.profile_name)
        profileImageView = view.findViewById(R.id.profile_image)
        settingsIcon = view.findViewById(R.id.settings_icon)
        logoutIcon = view.findViewById(R.id.logout_icon)
        recentActivityRestaurantName = view.findViewById(R.id.card1_restaurant_name)
        recentActivityDate = view.findViewById(R.id.card1_content)
        recentActivityMenu = view.findViewById(R.id.card1_menu)


        setupCardListeners(view)
        // 사용자 데이터 로드
        loadProfileData()

        // Recent Activity 데이터 로드
        loadRecentActivity()

        // Settings 버튼 클릭 리스너 설정
        setupSettingsButton()

        logoutIcon.setOnClickListener {
            showLogoutConfirmationDialog()
        }


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
            emailTextView.text = user.email ?: "No email available"
            nameTextView.text = user.displayName ?: "Default Name"

            val storageRef =
                FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                if (isAdded) {
                    Glide.with(requireContext())
                        .load(uri)
                        .placeholder(R.drawable.default_profile)
                        .into(profileImageView)
                }
            }.addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Failed to load profile image", exception)
                profileImageView.setImageResource(R.drawable.default_profile)
            }
        } else {
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


    private fun setupCardListeners(view: View) {
        Log.d("ProfileFragment", "Setting up card listeners") // 추가

        val expenseSummaryButton = view.findViewById<Button>(R.id.expense_summary_button)
        val restaurantHistoryButton = view.findViewById<Button>(R.id.restaurant_history_button)

        Log.d("ProfileFragment", "Buttons found: expense=${expenseSummaryButton != null}, history=${restaurantHistoryButton != null}") // 추가

        expenseSummaryButton?.setOnClickListener {
            Log.d("ProfileFragment", "Expense Summary button clicked")
            findNavController().navigate(R.id.action_profile_to_expense_detail)
        }

        restaurantHistoryButton?.setOnClickListener {
            Log.d("ProfileFragment", "Restaurant History button clicked")
            findNavController().navigate(R.id.action_profile_to_restaurant_history)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                performLogout()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun performLogout() {
        try {
            // Activity가 유효한지 확인
            if (!isAdded || activity == null) return

            // 세션 정보 삭제
            sessionManager.clearSession()

            // Firebase 로그아웃
            auth.signOut()

            // 로그인 화면으로 이동
            Intent(requireContext(), LoginActivity::class.java).also { intent ->
                // 새로운 태스크로 시작하고 이전 태스크를 모두 제거
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }

            // Activity 종료는 startActivity 후에 실행
            activity?.finish()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Logout error", e)
            Toast.makeText(requireContext(), "로그아웃 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
