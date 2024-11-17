package com.example.mychelin_page

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import com.example.mychelin_page.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuFragment : Fragment(R.layout.fragment_menu) {
    private lateinit var btnNotice: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var announcementText: TextView
    private lateinit var ratingsImage: ImageView
    private lateinit var btnFrequentQna: Button
    private lateinit var btnServiceInfo: Button
    private lateinit var btnNoticeLink: Button
    private lateinit var btnBugReport: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        // 뷰 초기화
        btnNotice = view.findViewById(R.id.btnNotice)
        btnSettings = view.findViewById(R.id.btnSettings)
        announcementText = view.findViewById(R.id.announcementText)
        ratingsImage = view.findViewById(R.id.ratingsImage)
        btnFrequentQna = view.findViewById(R.id.btnFrequentQna)
        btnServiceInfo = view.findViewById(R.id.btnServiceInfo)
        btnNoticeLink = view.findViewById(R.id.btnNoticeLink)
        btnBugReport = view.findViewById(R.id.btnBugReport)

        setupListeners()
        setupAnnouncementText()

        return view
    }

    private fun setupListeners() {
        // 알림 버튼
        btnNotice.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.navigation_graph, NoticeFragment())
                .addToBackStack(null)
                .commit()
        }

        // 설정 버튼
        btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        // FAQ 버튼
        btnFrequentQna.setOnClickListener {
            openUrl("https://www.naver.com/")
        }

        // 서비스 정보 버튼
        btnServiceInfo.setOnClickListener {
            openUrl("https://github.com/ynchaanST/ITM_Mobile")
        }

        // 공지사항 링크 버튼
        btnNoticeLink.setOnClickListener {
            openUrl("https://eclass.seoultech.ac.kr/")
        }

        // 버그 리포트 버튼
        btnBugReport.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.navigation_graph, ReportFragment())
                .addToBackStack(null)
                .commit()
        }

        val IcNotice = view?.findViewById<ImageButton>(R.id.btnNotice)
        IcNotice?.clearColorFilter()

        val IcSettings = view?.findViewById<ImageButton>(R.id.btnSettings)
        IcSettings?.clearColorFilter()
    }

    private fun setupAnnouncementText() {
        announcementText.text = "service launch since 2024"
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}