package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

class DashboardActivity : AppCompatActivity() {


    private lateinit var recentOrdersAdapter: CheckOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)



        // Setup Logo click as back button
        findViewById<ImageView>(R.id.ivAppLogo)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup Recent Orders RecyclerView
        val rvRecentOrders = findViewById<RecyclerView>(R.id.rvRecentOrders) ?: return
        ResponsiveUtils.setupResponsiveRecyclerView(this, rvRecentOrders, 350) // 350dp for dashboard items
        recentOrdersAdapter = CheckOrderAdapter(emptyList()) { order ->
            val intent = Intent(this, CustomerProfileActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", order.customerName)
            intent.putExtra("CUSTOMER_MOBILE", order.mobile)
            intent.putExtra("SELECTED_GARMENT", order.garmentType)
            startActivity(intent)
        }
        rvRecentOrders.adapter = recentOrdersAdapter

        // Dashboard Stat Card Clicks
        findViewById<android.view.View>(R.id.cardPendingOrders)?.setOnClickListener {
            val intent = Intent(this, CheckOrderActivity::class.java)
            intent.putExtra("ORDER_STATUS", "Pending")
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.cardActiveOrders)?.setOnClickListener {
            val intent = Intent(this, CheckOrderActivity::class.java)
            intent.putExtra("ORDER_STATUS", "In Progress")
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.cardCompletedOrders)?.setOnClickListener {
            val intent = Intent(this, CheckOrderActivity::class.java)
            intent.putExtra("ORDER_STATUS", "Completed")
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.activity_check_order)?.setOnClickListener {
            val intent = Intent(this, CheckOrderActivity::class.java)
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.cardAddCustomer)?.setOnClickListener {
            val intent = Intent(this, AddCustomerActivity::class.java)
            startActivity(intent)
        }

        updateStats()
        loadRecentOrders()
        checkAppStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStats()
        loadRecentOrders()
    }

    private fun updateStats() {
        val tvActive = findViewById<TextView>(R.id.tvActiveOrders)
        val tvPending = findViewById<TextView>(R.id.tvPendingOrders)
        val tvCompleted = findViewById<TextView>(R.id.tvCompletedOrders)
        val tvTotalCust = findViewById<TextView>(R.id.tvTotalCustomersCount)

        RetrofitClient.instance.getDashboardStats().enqueue(object : retrofit2.Callback<DashboardStatsResponse> {
            override fun onResponse(call: retrofit2.Call<DashboardStatsResponse>, response: retrofit2.Response<DashboardStatsResponse>) {
                if (response.isSuccessful) {
                    val stats = response.body()
                    tvActive?.text = stats?.activeOrders.toString()
                    tvPending?.text = stats?.pendingOrders.toString()
                    tvCompleted?.text = stats?.completedOrders.toString()
                    tvTotalCust?.text = stats?.totalCustomers.toString()
                } else {
                    android.util.Log.e("DASHBOARD", "Backend stats error: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<DashboardStatsResponse>, t: Throwable) {
                android.util.Log.e("DASHBOARD", "Network failure: ${t.message}")
            }
        })
    }

    private fun loadRecentOrders() {
        RetrofitClient.instance.getRecentOrders(5).enqueue(object : retrofit2.Callback<List<RecentOrderResponse>> {
            override fun onResponse(
                call: retrofit2.Call<List<RecentOrderResponse>>,
                response: retrofit2.Response<List<RecentOrderResponse>>
            ) {
                if (response.isSuccessful) {
                    val backendData = response.body() ?: emptyList()
                    val list = backendData.map {
                        CheckOrderModel(
                            it.id ?: "0", 
                            it.customerName ?: "Unknown", 
                            it.mobileNumber ?: "No Number", 
                            it.garmentType ?: "General"
                        )
                    }
                    recentOrdersAdapter.updateData(list)
                } else {
                    android.util.Log.e("DASHBOARD", "Backend recent orders error: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<RecentOrderResponse>>, t: Throwable) {
                android.util.Log.e("DASHBOARD", "Network failure: ${t.message}")
            }
        })
    }

    private fun checkAppStatus() {
        val tvAppStatus = findViewById<TextView>(R.id.tvAppStatus) ?: return
        
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
                    tvAppStatus.text = "v$currentVersion"
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
                                tvAppStatus.text = "Updated App (v$currentVersion)"
                                tvAppStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                            } else {
                                tvAppStatus.text = "Update Available (v$latestVersion)"
                                tvAppStatus.setTextColor(android.graphics.Color.parseColor("#FFEB3B"))
                            }
                        } catch (e: Exception) {
                            tvAppStatus.text = "v$currentVersion"
                        }
                    } else {
                        tvAppStatus.text = "v$currentVersion"
                    }
                }
            }
        })
    }
}
