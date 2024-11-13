package com.example.mychelin_page

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mychelin_page.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 초기 화면 프래그먼트 설정 (HomeFragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        // 네비게이션 바 버튼 설정
        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            replaceFragment(MenuFragment())
        }
        findViewById<ImageButton>(R.id.search_button).setOnClickListener {
            replaceFragment(SearchFragment())
        }
        findViewById<ImageButton>(R.id.home_button).setOnClickListener {
            replaceFragment(HomeFragment())
        }
        findViewById<ImageButton>(R.id.booking_button).setOnClickListener {
            replaceFragment(BookingFragment())
        }
        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            replaceFragment(SettingsFragment())
        }
    }

    // 프래그먼트를 교체하는 함수
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    }
}