package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request

class AdminProfileActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.`admin_profile`)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileMobile = findViewById<TextView>(R.id.tvProfileMobile)
        val btnLogout = findViewById<MaterialButton>(R.id.btnProfileLogout)

        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        tvProfileName?.text = sharedPref.getString("USER_NAME", "Tailor Master")
        tvProfileMobile?.text = sharedPref.getString("USER_MOBILE", "+91 0000000000")

        btnBack?.setOnClickListener {
            animateButtonClick(it)
            it.postDelayed({ onBackPressedDispatcher.onBackPressed() }, 150)
        }

        // Logout is hidden as we are now offline-only without a central server account
        btnLogout?.visibility = View.GONE

        val btnThemeToggle = findViewById<MaterialButton>(R.id.btnThemeToggle)
        val isDarkMode = sharedPref.getBoolean("IS_DARK_MODE", false)
        btnThemeToggle?.text = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode"
        
        btnThemeToggle?.setOnClickListener {
            animateButtonClick(it)
            val nextMode = !sharedPref.getBoolean("IS_DARK_MODE", false)
            sharedPref.edit().putBoolean("IS_DARK_MODE", nextMode).apply()
            
            it.postDelayed({
                AppCompatDelegate.setDefaultNightMode(
                    if (nextMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                recreate()
            }, 150)
        }

        checkAppStatus()

        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_home)

        // Hide or remove Drive buttons as they are no longer supported in totelly offline mode
        findViewById<MaterialButton>(R.id.btnBackupNow)?.visibility = View.GONE
        findViewById<MaterialButton>(R.id.btnRestoreFromDrive)?.visibility = View.GONE
    }

    private fun animateButtonClick(view: View) {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
        view.animate().cancel()
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(120).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }.start()
    }

    private fun checkAppStatus() {
        val tvAppVersion = findViewById<TextView>(R.id.tvAppVersion)
        val currentContext = this
        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) { "1.0" }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/dharmik264/Dhandhukiya-tailor-/releases")
            .build()
            
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread { tvAppVersion?.text = "Version: v$currentVersion" }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseData != null) {
                        try {
                            val jsonArray = org.json.JSONArray(responseData)
                            if (jsonArray.length() == 0) return@runOnUiThread
                            val latestVersion = jsonArray.getJSONObject(0).getString("tag_name").removePrefix("v")
                            tvAppVersion?.text = if (latestVersion == currentVersion) "Updated Application (v$currentVersion)" 
                                                else "Update Available: v$latestVersion"
                        } catch (e: Exception) { tvAppVersion?.text = "Version: v$currentVersion" }
                    } else { tvAppVersion?.text = "Version: v$currentVersion" }
                }
            }
        })
    }
}
