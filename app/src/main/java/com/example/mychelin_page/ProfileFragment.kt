package com.example.mychelin_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var settingsIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize the settings icon
        settingsIcon = view.findViewById(R.id.settings_icon)

        // Set up click listener for the settings icon
        setupSettingsButton()

        return view
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