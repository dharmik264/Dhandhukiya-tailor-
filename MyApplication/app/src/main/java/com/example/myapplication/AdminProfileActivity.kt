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
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request

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

        checkAppStatus()
    }

    private fun checkAppStatus() {
        val tvAppVersion = findViewById<TextView>(R.id.tvAppVersion)
        
        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/dharmik264/Dhandhukiya-tailor-/releases")
            .build()
            
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    tvAppVersion.text = "Version: v$currentVersion"
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseData != null) {
                        try {
                            val jsonArray = org.json.JSONArray(responseData)
                            if (jsonArray.length() == 0) return@runOnUiThread
                            val json = jsonArray.getJSONObject(0)
                            val latestVersion = json.getString("tag_name").removePrefix("v")
                            
                            val isUpdateAvailable = try {
                                val cParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
                                val lParts = latestVersion.split(".").mapNotNull { it.toIntOrNull() }
                                var newer = latestVersion != currentVersion && lParts.isNotEmpty()
                                if (lParts.isNotEmpty() && cParts.isNotEmpty()) {
                                    for (i in 0 until maxOf(cParts.size, lParts.size)) {
                                        val c = cParts.getOrElse(i) { 0 }
                                        val l = lParts.getOrElse(i) { 0 }
                                        if (l > c) { newer = true; break }
                                        if (c > l) { newer = false; break }
                                    }
                                }
                                newer
                            } catch (e: Exception) {
                                latestVersion != currentVersion
                            }
                            
                            if (!isUpdateAvailable) {
                                tvAppVersion.text = "Updated Application (v$currentVersion)"
                                tvAppVersion.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                            } else {
                                tvAppVersion.text = "Update Available: v$latestVersion (Current: v$currentVersion)"
                                tvAppVersion.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                            }
                        } catch (e: Exception) {
                            tvAppVersion.text = "Version: v$currentVersion"
                        }
                    } else {
                        tvAppVersion.text = "Version: v$currentVersion"
                    }
                }
            }
        })
    }
}
