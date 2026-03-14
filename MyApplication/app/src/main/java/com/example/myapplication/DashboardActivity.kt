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
        val rvRecentOrders = findViewById<RecyclerView>(R.id.rvRecentOrders)
        rvRecentOrders.layoutManager = LinearLayoutManager(this)
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


}
