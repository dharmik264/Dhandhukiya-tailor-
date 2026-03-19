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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        ResponsiveUtils.setupResponsiveRecyclerView(this, rvRecentOrders, 350) 
        
        recentOrdersAdapter = CheckOrderAdapter(emptyList()) { order ->
            val intent = Intent(this, CustomerProfileActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", order.customerName)
            intent.putExtra("CUSTOMER_MOBILE", order.mobileNumber)
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

        findViewById<android.view.View>(R.id.cardBackupData)?.setOnClickListener {
            lifecycleScope.launch {
                OfflineBackupUtils.exportDataToCSV(this@DashboardActivity)
            }
        }

        updateStats()
        loadRecentOrders()
        
        // App status is now just version display
        val tvAppStatus = findViewById<TextView>(R.id.tvAppStatus)
        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
        tvAppStatus?.text = "Offline Mode (v$currentVersion)"
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

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@DashboardActivity)
                val allCustomers = db.customerDao().getAll()
                val allMeasurements = db.measurementDao().getAllMeasurements()
                
                val totalCount = allCustomers.size
                
                // Calculate stats from measurements status
                val pendingCount = allMeasurements.count { it.status.equals("Pending", ignoreCase = true) }
                val activeCount = allMeasurements.count { it.status.equals("In Progress", ignoreCase = true) }
                val completedCount = allMeasurements.count { it.status.equals("Completed", ignoreCase = true) }

                withContext(Dispatchers.Main) {
                    tvTotalCust?.text = totalCount.toString()
                    tvPending?.text = pendingCount.toString()
                    tvActive?.text = activeCount.toString()
                    tvCompleted?.text = completedCount.toString()
                }
            } catch (e: Exception) {
                android.util.Log.e("DASHBOARD", "Stats update error: ${e.message}")
            }
        }
    }

    private fun loadRecentOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@DashboardActivity)
                // Get 5 recent measurements from local database
                val recentMeasurements = db.measurementDao().getRecentMeasurements(5)
                
                val list = recentMeasurements.map { m ->
                    // Find customer for each measurement to get the name
                    val customer = db.customerDao().getAll().find { it.mobileNumber == m.customerMobile }
                    CheckOrderModel(
                        m.id.toString(),
                        customer?.name ?: "Unknown",
                        m.customerMobile,
                        m.garmentType
                    )
                }

                withContext(Dispatchers.Main) {
                    recentOrdersAdapter.updateData(list)
                }
            } catch (e: Exception) {
                android.util.Log.e("DASHBOARD", "Local recent orders error: ${e.message}")
            }
        }
    }
}
