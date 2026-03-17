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

class CheckOrderActivity : AppCompatActivity() {

    private lateinit var rvCheckOrders: RecyclerView
    private lateinit var adapter: CheckOrderAdapter
    private var filterStatus: String? = null
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_order)

        rvCheckOrders = findViewById(R.id.rvCheckOrders) ?: return
        rvCheckOrders.layoutManager = LinearLayoutManager(this)
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
            val intent = Intent(this, CustomerProfileActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", order.customerName)
            intent.putExtra("CUSTOMER_MOBILE", order.mobile)
            intent.putExtra("SELECTED_GARMENT", order.garmentType)
            startActivity(intent)
        }
        rvCheckOrders.adapter = adapter

        val navBar = findViewById<BottomNavigationView>(R.id.mainBottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_reports)

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
        RetrofitClient.instance.getRecentOrders(1000, filterStatus).enqueue(object : retrofit2.Callback<List<RecentOrderResponse>> {
            override fun onResponse(
                call: retrofit2.Call<List<RecentOrderResponse>>,
                response: retrofit2.Response<List<RecentOrderResponse>>
            ) {
                if (response.isSuccessful) {
                    val backendData = response.body() ?: emptyList()
                    val list = backendData.map {
                        CheckOrderModel(
                            id = it.id ?: "0",
                            customerName = it.customerName ?: "Unknown",
                            mobile = it.mobileNumber ?: "",
                            garmentType = it.garmentType ?: "Shirt",
                            orderDate = it.orderDate ?: "",
                            status = it.status ?: ""
                        )
                    }
                    adapter.updateData(list)
                } else {
                    Log.e("CHECK_ORDER_ERROR", "Backend orders error: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<RecentOrderResponse>>, t: Throwable) {
                Log.e("CHECK_ORDER_ERROR", "Network failure: ${t.message}")
            }
        })
    }
}