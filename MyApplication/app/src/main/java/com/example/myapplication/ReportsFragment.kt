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
import com.google.android.material.imageview.ShapeableImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        
        // Hide the navigation bar because it's now in the activity
        view.findViewById<View>(R.id.bottomNavigation)?.visibility = View.GONE

        // Handle Logo Click as Back (Navigate to Home tab)
        view.findViewById<android.widget.ImageView>(R.id.ivAppLogo)?.setOnClickListener {
            (activity as? MainActivity)?.viewPager?.currentItem = 0
        }

        // Setup Quick Actions
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

        // Setup Recent Orders RecyclerView
        val rvRecentOrders = view.findViewById<RecyclerView>(R.id.rvRecentOrders)
        val currentContext = context ?: return view
        rvRecentOrders.layoutManager = LinearLayoutManager(currentContext)
        recentOrdersAdapter = CheckOrderAdapter(emptyList()) { order ->
            context?.let { ctx ->
                val intent = Intent(ctx, CustomerProfileActivity::class.java)
                intent.putExtra("CUSTOMER_NAME", order.customerName)
                intent.putExtra("CUSTOMER_MOBILE", order.mobile)
                intent.putExtra("SELECTED_GARMENT", order.garmentType)
                startActivity(intent)
            }
        }
        rvRecentOrders.adapter = recentOrdersAdapter

        // Dashboard Stat Card Clicks
        view.findViewById<View>(R.id.cardPendingOrders)?.setOnClickListener {
            context?.let { ctx ->
                val intent = Intent(ctx, CheckOrderActivity::class.java)
                intent.putExtra("ORDER_STATUS", "Pending")
                startActivity(intent)
            }
        }
        view.findViewById<View>(R.id.cardActiveOrders)?.setOnClickListener {
            context?.let { ctx ->
                val intent = Intent(ctx, CheckOrderActivity::class.java)
                intent.putExtra("ORDER_STATUS", "In Progress")
                startActivity(intent)
            }
        }
        view.findViewById<View>(R.id.cardCompletedOrders)?.setOnClickListener {
            context?.let { ctx ->
                val intent = Intent(ctx, CheckOrderActivity::class.java)
                intent.putExtra("ORDER_STATUS", "Completed")
                startActivity(intent)
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
        RetrofitClient.instance.getDashboardStats().enqueue(object : Callback<DashboardStatsResponse> {
            override fun onResponse(call: Call<DashboardStatsResponse>, response: Response<DashboardStatsResponse>) {
                if (isAdded && response.isSuccessful) {
                    val stats = response.body()
                    tvActive.text = stats?.activeOrders.toString()
                    tvPending.text = stats?.pendingOrders.toString()
                    tvCompleted.text = stats?.completedOrders.toString()
                    tvTotalCust.text = stats?.totalCustomers.toString()
                }
            }
            override fun onFailure(call: Call<DashboardStatsResponse>, t: Throwable) {
                Log.e("REPORTS_FRAG", "Network failure: ${t.message}")
            }
        })
    }

    private fun loadRecentOrders() {
        RetrofitClient.instance.getRecentOrders(5).enqueue(object : Callback<List<RecentOrderResponse>> {
            override fun onResponse(call: Call<List<RecentOrderResponse>>, response: Response<List<RecentOrderResponse>>) {
                if (isAdded && response.isSuccessful) {
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
                    recentOrdersAdapter.updateData(list)
                }
            }
            override fun onFailure(call: Call<List<RecentOrderResponse>>, t: Throwable) {
                Log.e("REPORTS_FRAG", "Network failure: ${t.message}")
            }
        })
    }
}
