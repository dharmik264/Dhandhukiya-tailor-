package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerListActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var rvCustomers: RecyclerView
    private lateinit var adapter: CustomerAdapter
    private var allCustomers = mutableListOf<CustomerDisplayModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        tvTitle = findViewById(R.id.tvTitle)
        rvCustomers = findViewById(R.id.rvCustomers)

        ResponsiveUtils.setupResponsiveRecyclerView(this, rvCustomers, 300)
        
        adapter = CustomerAdapter(emptyList(), { customer ->
            val intent = Intent(this, CustomerProfileActivity::class.java).apply {
                putExtra("CUSTOMER_NAME", customer.name)
                putExtra("CUSTOMER_MOBILE", customer.mobileNumber)
                putExtra("SELECTED_GARMENT", "Shirt")
            }
            startActivity(intent)
        }, { customer ->
            val intent = Intent(this, AddCustomerActivity::class.java).apply {
                putExtra("CUSTOMER_NAME", customer.name)
                putExtra("CUSTOMER_MOBILE", customer.mobileNumber)
                putExtra("IS_EDIT_MODE", true)
            }
            startActivity(intent)
        }, { mobile ->
            showDeleteConfirmation(mobile)
        })
        rvCustomers.adapter = adapter

        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_customers)
        
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@CustomerListActivity)
                val customers = withContext(Dispatchers.IO) {
                    db.customerDao().getAll()
                }
                
                val newList = customers.map { 
                    CustomerDisplayModel(
                        id = it.id.toString(), 
                        name = it.name, 
                        mobileNumber = it.mobileNumber, 
                        length = it.length,
                        status = it.status
                    ) 
                }
                allCustomers = newList.sortedBy { it.name.lowercase() }.toMutableList()
                adapter.updateData(allCustomers)
                tvTitle.text = "Registered Customers (${allCustomers.size})"
            } catch (e: Exception) {
                Log.e("CUST_LIST", "Refresh error: ${e.message}")
            }
        }
    }

    private fun showDeleteConfirmation(mobile: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Customer")
            .setMessage("Permanently remove this customer and all their measurements from your phone?")
            .setPositiveButton("Delete") { _, _ -> 
                lifecycleScope.launch {
                    try {
                        val db = AppDatabase.getDatabase(this@CustomerListActivity)
                        withContext(Dispatchers.IO) {
                            val customer = db.customerDao().getAll().find { it.mobileNumber == mobile }
                            if (customer != null) {
                                db.customerDao().delete(customer)
                            }
                        }
                        refreshData()
                        Toast.makeText(this@CustomerListActivity, "Deleted", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@CustomerListActivity, "Error deleting", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}