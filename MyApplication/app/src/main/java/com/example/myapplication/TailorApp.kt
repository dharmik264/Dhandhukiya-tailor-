package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
class TailorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Globally set theme once at startup
        val pref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isDarkMode = pref.getBoolean("IS_DARK_MODE", false)
        val targetMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
    }
}
