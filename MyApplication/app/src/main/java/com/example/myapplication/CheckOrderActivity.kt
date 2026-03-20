package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckOrderActivity : AppCompatActivity() {

    private lateinit var rvCheckOrders: RecyclerView
    private lateinit var adapter: CheckOrderAdapter
    private var filterStatus: String? = null
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_order)

        rvCheckOrders = findViewById(R.id.rvCheckOrders) ?: return
        ResponsiveUtils.setupResponsiveRecyclerView(this, rvCheckOrders, 350)
        tvTitle = findViewById(R.id.tvCheckOrderTitle) ?: return

        filterStatus = intent.getStringExtra("ORDER_STATUS")
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupFilters) ?: return

        // Set initial chip based on intent
        when (filterStatus) {
            "Pending" -> chipGroup.check(R.id.chipPending)
            "In Progress" -> chipGroup.check(R.id.chipInProgress)
            "Completed" -> chipGroup.check(R.id.chipCompleted)
            else -> chipGroup.check(R.id.chipAll)
        }

        if (filterStatus != null) {
            tvTitle.text = "$filterStatus Orders"
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull()
            filterStatus = when (id) {
                R.id.chipPending -> "Pending"
                R.id.chipInProgress -> "In Progress"
                R.id.chipCompleted -> "Completed"
                else -> null
            }
            tvTitle.text = if (filterStatus == null) "Check Orders" else "$filterStatus Orders"
            loadAllOrders()
        }

        adapter = CheckOrderAdapter(emptyList()) { order ->
            val intent = Intent(this, CustomerProfileActivity::class.java).apply {
                putExtra("CUSTOMER_NAME", order.customerName)
                putExtra("CUSTOMER_MOBILE", order.mobileNumber)
                putExtra("SELECTED_GARMENT", order.garmentType)
            }
            startActivity(intent)
        }
        rvCheckOrders.adapter = adapter

        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_reports)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadAllOrders()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.selectedItemId = R.id.nav_reports
        loadAllOrders()
    }

    private fun loadAllOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@CheckOrderActivity)
                val allMeasurements = db.measurementDao().getAllMeasurements()
                val allCustomers = db.customerDao().getAll()
                
                val filteredMeasurements = if (filterStatus == null) {
                    allMeasurements
                } else {
                    allMeasurements.filter { it.status.equals(filterStatus, ignoreCase = true) }
                }

                val displayList = filteredMeasurements.map { m ->
                    val customer = allCustomers.find { it.mobileNumber == m.customerMobile }
                    CheckOrderModel(
                        id = m.id.toString(),
                        customerName = customer?.name ?: "Unknown",
                        mobileNumber = m.customerMobile,
                        garmentType = m.garmentType,
                        orderDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(m.updatedAt)),
                        status = m.status
                    )
                }

                withContext(Dispatchers.Main) {
                    adapter.updateData(displayList.sortedByDescending { it.id.toInt() })
                }
            } catch (e: Exception) {
                Log.e("CHECK_ORDER_ERROR", "Local order loading error: ${e.message}")
            }
        }
    }
}