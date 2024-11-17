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
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mychelin_page.R

class MenuFragment : Fragment() {
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

        initializeViews(view)
        setupListeners()
        setupAnnouncementText()

        return view
    }

    private fun initializeViews(view: View) {
        btnNotice = view.findViewById(R.id.btnNotice)
        btnSettings = view.findViewById(R.id.btnSettings)
        announcementText = view.findViewById(R.id.announcementText)
        ratingsImage = view.findViewById(R.id.ratingsImage)
        btnFrequentQna = view.findViewById(R.id.btnFrequentQna)
        btnServiceInfo = view.findViewById(R.id.btnServiceInfo)
        btnNoticeLink = view.findViewById(R.id.btnNoticeLink)
        btnBugReport = view.findViewById(R.id.btnBugReport)
    }

    private fun setupListeners() {
        btnNotice.setOnClickListener {
            try {
                findNavController().navigate(R.id.menu_notice)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open Notice: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnSettings.setOnClickListener {
            try {
                val intent = Intent(requireContext(), SettingsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open Settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnFrequentQna.setOnClickListener { openUrl("https://www.naver.com/") }
        btnServiceInfo.setOnClickListener { openUrl("https://github.com/ynchaanST/ITM_Mobile") }
        btnNoticeLink.setOnClickListener { openUrl("https://eclass.seoultech.ac.kr/") }

        btnBugReport.setOnClickListener {
            try {
                findNavController().navigate(R.id.menu_report)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open Bug Report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAnnouncementText() {
        announcementText.text = "service launch since 2024"
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open URL: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}