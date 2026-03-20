package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportsFragment : Fragment() {

    private lateinit var recentOrdersAdapter: CheckOrderAdapter
    private lateinit var tvActive: TextView
    private lateinit var tvPending: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvTotalCust: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_dashboard, container, false)

        // Fragments shouldn't show the internal navbar as MainActivity provides one
        view.findViewById<View>(R.id.bottomNavigation)?.visibility = View.GONE
        
        view.findViewById<android.widget.ImageView>(R.id.ivAppLogo)?.setOnClickListener {
            (activity as? MainActivity)?.viewPager?.currentItem = 0
        }

        view.findViewById<MaterialCardView>(R.id.activity_check_order)?.setOnClickListener {
            context?.let { ctx ->
                startActivity(Intent(ctx, CheckOrderActivity::class.java))
            }
        }

        view.findViewById<MaterialCardView>(R.id.cardAddCustomer)?.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(1)
        }

        view.findViewById<android.widget.FrameLayout>(R.id.ivProfileBtn)?.setOnClickListener {
            context?.let { ctx ->
                startActivity(Intent(ctx, AdminProfileActivity::class.java))
            }
        }

        tvActive = view.findViewById(R.id.tvActiveOrders)
        tvPending = view.findViewById(R.id.tvPendingOrders)
        tvCompleted = view.findViewById(R.id.tvCompletedOrders)
        tvTotalCust = view.findViewById(R.id.tvTotalCustomersCount)

        val rvRecentOrders = view.findViewById<RecyclerView>(R.id.rvRecentOrders)
        val currentContext = context ?: return view
        rvRecentOrders.layoutManager = LinearLayoutManager(currentContext)
        recentOrdersAdapter = CheckOrderAdapter(emptyList()) { order ->
            context?.let { ctx ->
                val intent = Intent(ctx, CustomerProfileActivity::class.java).apply {
                    putExtra("CUSTOMER_NAME", order.customerName)
                    putExtra("CUSTOMER_MOBILE", order.mobileNumber)
                    putExtra("SELECTED_GARMENT", order.garmentType)
                }
                startActivity(intent)
            }
        }
        rvRecentOrders.adapter = recentOrdersAdapter

        view.findViewById<View>(R.id.cardPendingOrders)?.setOnClickListener {
            context?.let { ctx ->
                val intent = Intent(ctx, CheckOrderActivity::class.java).apply {
                    putExtra("ORDER_STATUS", "Pending")
                }
                startActivity(intent)
            }
        }
        view.findViewById<View>(R.id.cardActiveOrders)?.setOnClickListener {
            context?.let { ctx ->
                val intent = Intent(ctx, CheckOrderActivity::class.java).apply {
                    putExtra("ORDER_STATUS", "In Progress")
                }
                startActivity(intent)
            }
        }
        view.findViewById<View>(R.id.cardCompletedOrders)?.setOnClickListener {
            context?.let { ctx ->
                val intent = Intent(ctx, CheckOrderActivity::class.java).apply {
                    putExtra("ORDER_STATUS", "Completed")
                }
                startActivity(intent)
            }
        }

        view.findViewById<TextView>(R.id.tvViewAll)?.setOnClickListener {
            context?.let { ctx ->
                startActivity(Intent(ctx, CheckOrderActivity::class.java))
            }
        }

        updateStats()
        loadRecentOrders()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateStats()
        loadRecentOrders()
    }

    private fun updateStats() {
        val currentContext = context ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(currentContext)
                val allCustomers = db.customerDao().getAll()
                val allMeasurements = db.measurementDao().getAllMeasurements()
                
                val totalCount = allCustomers.size
                val pendingCount = allMeasurements.count { it.status.equals("Pending", ignoreCase = true) }
                val activeCount = allMeasurements.count { it.status.equals("In Progress", ignoreCase = true) }
                val completedCount = allMeasurements.count { it.status.equals("Completed", ignoreCase = true) }

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        tvTotalCust.text = totalCount.toString()
                        tvPending.text = pendingCount.toString()
                        tvActive.text = activeCount.toString()
                        tvCompleted.text = completedCount.toString()
                    }
                }
            } catch (e: Exception) {
                Log.e("REPORTS_FRAG", "Local stats update error: ${e.message}")
            }
        }
    }

    private fun loadRecentOrders() {
        val currentContext = context ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(currentContext)
                val recentMeasurements = db.measurementDao().getRecentMeasurements(5)
                val allCustomers = db.customerDao().getAll()
                
                val list = recentMeasurements.map { m ->
                    val customer = allCustomers.find { it.mobileNumber == m.customerMobile }
                    CheckOrderModel(
                        id = m.id.toString(),
                        customerName = customer?.name ?: "Unknown",
                        mobileNumber = m.customerMobile,
                        garmentType = m.garmentType,
                        orderDate = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(m.updatedAt)),
                        status = m.status
                    )
                }

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        recentOrdersAdapter.updateData(list)
                    }
                }
            } catch (e: Exception) {
                Log.e("REPORTS_FRAG", "Local orders loading error: ${e.message}")
            }
        }
    }
}
