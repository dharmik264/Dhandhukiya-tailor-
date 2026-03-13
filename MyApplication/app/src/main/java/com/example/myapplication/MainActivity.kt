package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var bottomNavigation: BottomNavigationView
    private var isSyncing = false

    fun navigateToTab(index: Int) {
        viewPager.currentItem = index
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        bottomNavigation = findViewById(R.id.mainBottomNavigation)

        // Setup ViewPager Adapter
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> HomeFragment()
                    1 -> AddCustomerFragment()
                    2 -> ReportsFragment()
                    else -> HomeFragment()
                }
            }
        }
        
        viewPager.isUserInputEnabled = true
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapter

        // Link ViewPager and BottomNavigation with guards to prevent loops
        bottomNavigation.setOnItemSelectedListener { item ->
            if (isSyncing) return@setOnItemSelectedListener true
            val targetPos = when (item.itemId) {
                R.id.nav_home -> 0
                R.id.nav_customers -> 1
                R.id.nav_reports -> 2
                else -> -1
            }
            if (targetPos != -1 && viewPager.currentItem != targetPos) {
                isSyncing = true
                viewPager.setCurrentItem(targetPos, true) // Smooth scroll highlights the swipe action
                isSyncing = false
            }
            true
        }

        // Handle page swipes to update current item in bottom nav
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (isSyncing) return
                val targetId = when (position) {
                    0 -> R.id.nav_home
                    1 -> R.id.nav_customers
                    2 -> R.id.nav_reports
                    else -> -1
                }
                if (targetId != -1 && bottomNavigation.selectedItemId != targetId) {
                    isSyncing = true
                    bottomNavigation.selectedItemId = targetId
                    isSyncing = false
                }
                
                // Ensure bottom navigation is visible across all primary tabs
                bottomNavigation.visibility = android.view.View.VISIBLE
            }
        })

        // Ensure initial state or external navigation is handled
        val externalTab = intent?.getIntExtra("TARGET_TAB", -1) ?: -1
        if (externalTab != -1) {
            viewPager.post {
                isSyncing = true
                viewPager.setCurrentItem(externalTab, true)
                isSyncing = false
            }
        } else if (savedInstanceState == null) {
            viewPager.post {
                isSyncing = true
                viewPager.setCurrentItem(0, false)
                bottomNavigation.selectedItemId = R.id.nav_home
                isSyncing = false
            }
        }


    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val targetTab = intent.getIntExtra("TARGET_TAB", -1)
        if (targetTab != -1) {
            isSyncing = true
            viewPager.setCurrentItem(targetTab, true)
            isSyncing = false
        }
    }
}
