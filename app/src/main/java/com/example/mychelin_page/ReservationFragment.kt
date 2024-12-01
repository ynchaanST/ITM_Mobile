package com.example.mychelin_page

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mychelin_page.R

class ReservationFragment : Fragment(R.layout.fragment_reservation) {

    private lateinit var btnBooking: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reservation, container, false)

        initializeViews(view)
        setupListeners()

        return view
    }


    private fun initializeViews(view: View) {
        btnBooking = view.findViewById(R.id.resConfirmButton)
    }

    private fun setupListeners() {
        btnBooking.setOnClickListener {
            try {
                findNavController().navigate(R.id.page_booking)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open Notice: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}