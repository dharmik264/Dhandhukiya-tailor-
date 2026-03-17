package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            val intent = Intent(this, CustomerProfileActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", customer.name)
            intent.putExtra("CUSTOMER_MOBILE", customer.mobile)
            startActivity(intent)
        }, { customer ->
            val intent = Intent(this, AddCustomerActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", customer.name)
            intent.putExtra("CUSTOMER_MOBILE", customer.mobile)
            intent.putExtra("IS_EDIT_MODE", true)
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
        RetrofitClient.instance.getAllCustomers().enqueue(object : Callback<List<CustomerResponse>> {
            override fun onResponse(call: Call<List<CustomerResponse>>, response: Response<List<CustomerResponse>>) {
                if (response.isSuccessful) {
                    val backendData = response.body() ?: emptyList()
                    val newList = backendData.map { 
                        CustomerDisplayModel(
                            id = it.id?.toString() ?: "0", 
                            name = it.name ?: "Unknown", 
                            mobile = it.mobileNumber ?: "No Number", 
                            length = it.length ?: "",
                            status = it.status ?: "Pending"
                        ) 
                    }
                    allCustomers = newList.sortedBy { it.name.lowercase() }.toMutableList()
                    adapter.updateData(allCustomers)
                    tvTitle.text = "Registered Customers (${allCustomers.size})"
                }
            }

            override fun onFailure(call: Call<List<CustomerResponse>>, t: Throwable) {
                Log.e("CUST_LIST", "Error: ${t.message}")
                Toast.makeText(this@CustomerListActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(mobile: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Customer")
            .setMessage("Permanently remove this customer?")
            .setPositiveButton("Delete") { _, _ -> 
                RetrofitClient.instance.deleteCustomer(mobile).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        refreshData()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        refreshData()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}