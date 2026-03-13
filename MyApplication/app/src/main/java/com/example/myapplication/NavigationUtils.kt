package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setupGlobalNavigation(activity: Activity, currentItemId: Int) {
    this.selectedItemId = currentItemId
    this.setOnItemSelectedListener { item ->
        if (item.itemId == currentItemId) return@setOnItemSelectedListener true

        val targetTab = when (item.itemId) {
            R.id.nav_home -> 0
            R.id.nav_customers -> 1
            R.id.nav_reports -> 2
            else -> -1
        }

        if (targetTab != -1) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("TARGET_TAB", targetTab)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            activity.startActivity(intent)
            if (activity !is MainActivity) activity.finish()
            true
        } else {
            false
        }
    }
}