package com.example.mychelin_page

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mychelin_page.databinding.FragmentReportBinding
import com.google.android.datatransport.BuildConfig

class ReportFragment : Fragment(R.layout.fragment_report) {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReportBinding.bind(view)

        initViews()
    }

    private fun initViews() {
        binding.btnSubmitReport.setOnClickListener {
            sendErrorReportByEmail()
        }
    }

    private fun sendErrorReportByEmail() {
        val errorDescription = binding.etErrorDescription.text.toString().trim()

        if (errorDescription.isEmpty()) {
            Toast.makeText(requireContext(), "오류 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("dreamscape156@naver.com"))
            putExtra(Intent.EXTRA_SUBJECT, "앱 오류 신고")
            putExtra(Intent.EXTRA_TEXT, """
                오류 내용:
                ${errorDescription}
                
                기기 정보:
                - 모델: ${Build.MODEL}
                - OS 버전: ${Build.VERSION.RELEASE}
                - 앱 버전: ${BuildConfig.VERSION_NAME}
            """.trimIndent())
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "이메일 앱 선택"))
            binding.etErrorDescription.text.clear()
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "이메일 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}