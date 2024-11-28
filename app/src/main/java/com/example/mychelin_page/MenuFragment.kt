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
    private lateinit var announcementText: TextView
    private lateinit var ratingsImage: ImageView
    private lateinit var btn1: ImageButton
    private lateinit var btn2: ImageButton
    private lateinit var btn3: ImageButton
    private lateinit var btnFrequentQna: Button
    private lateinit var btnServiceInfo: Button
    private lateinit var btnReport: Button

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
        announcementText = view.findViewById(R.id.announcementText)
        ratingsImage = view.findViewById(R.id.ratingsImage)
        btn1 = view.findViewById(R.id.btn1)
        btn2 = view.findViewById(R.id.btn2)
        btn3 = view.findViewById(R.id.btn3)
        btnFrequentQna = view.findViewById(R.id.btnFrequentQna)
        btnServiceInfo = view.findViewById(R.id.btnServiceInfo)
        btnReport = view.findViewById(R.id.btnReport)
    }

    private fun setupListeners() {
        btnNotice.setOnClickListener {
            try {
                findNavController().navigate(R.id.page_notice)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open Notice: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnReport.setOnClickListener {
            try {
                findNavController().navigate(R.id.page_report)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open Notice: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Added ratings button listeners (you can customize these)
        btn1.setOnClickListener {
            Toast.makeText(context, "Rating 1 selected", Toast.LENGTH_SHORT).show()
        }
        btn2.setOnClickListener {
            Toast.makeText(context, "Rating 2 selected", Toast.LENGTH_SHORT).show()
        }
        btn3.setOnClickListener {
            Toast.makeText(context, "Rating 3 selected", Toast.LENGTH_SHORT).show()
        }

        btnFrequentQna.setOnClickListener { openUrl("https://docs.google.com/document/d/1b5brPzeRmaIxmSku4866Pm5ZE8C1NVdCSQUSWkED22o/edit?tab=t.0") }
        btnServiceInfo.setOnClickListener { openUrl("https://github.com/ynchaanST/ITM_Mobile") }
    }

    private fun setupAnnouncementText() {
        announcementText.text = "Mychelin Page Service Launch! 많관부~"
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