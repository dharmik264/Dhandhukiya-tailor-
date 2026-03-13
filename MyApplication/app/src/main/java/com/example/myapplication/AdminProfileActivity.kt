package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_profile)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileMobile = findViewById<TextView>(R.id.tvProfileMobile)
        val btnLogout = findViewById<MaterialButton>(R.id.btnProfileLogout)

        // Load data from SharedPreferences
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "Tailor Master")
        val userMobile = sharedPref.getString("USER_MOBILE", "+91 0000000000")

        tvProfileName.text = userName
        tvProfileMobile.text = userMobile

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnLogout.setOnClickListener {
            // Logout logic
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnThemeToggle = findViewById<MaterialButton>(R.id.btnThemeToggle)
        val isDarkMode = sharedPref.getBoolean("IS_DARK_MODE", false)
        
        btnThemeToggle.text = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode"
        
        btnThemeToggle.setOnClickListener {
            val currentlyDark = sharedPref.getBoolean("IS_DARK_MODE", false)
            val nextMode = !currentlyDark
            
            sharedPref.edit().putBoolean("IS_DARK_MODE", nextMode).apply()
            
            if (nextMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            
            // Recreate activity to apply theme change
            recreate()
        }

    }
}
