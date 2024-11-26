package com.example.mychelin_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // FirebaseAuth 초기화
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            // 이메일 설정
            val emailTextView = view.findViewById<TextView>(R.id.profile_email)
            emailTextView.text = user.email ?: "No email available"

            // 이름 설정
            val nameTextView = view.findViewById<TextView>(R.id.profile_name)
            nameTextView.text = user.displayName ?: "No name available"

            // Firebase Storage에서 프로필 사진 가져오기
            val profileImageView = view.findViewById<ImageView>(R.id.profile_image)
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.default_profile) // 기본 프로필 이미지
                    .into(profileImageView)
            }.addOnFailureListener {
                profileImageView.setImageResource(R.drawable.default_profile) // 실패 시 기본 이미지
            }
        }

        return view
    }
}