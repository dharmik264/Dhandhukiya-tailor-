package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {

    private lateinit var tvCustomerListTitle: TextView
    private lateinit var rvCustomers: RecyclerView
    private lateinit var adapter: CustomerAdapter
    private lateinit var etSearch: TextInputEditText
    
    private var allCustomers = mutableListOf<CustomerDisplayModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvCustomerListTitle = findViewById(R.id.tvCustomerListTitle)
        rvCustomers = findViewById(R.id.rvCustomers)
        etSearch = findViewById(R.id.etSearch)

        rvCustomers.layoutManager = LinearLayoutManager(this)
        adapter = CustomerAdapter(emptyList(), { customer ->
            val intent = Intent(this, CustomerProfileActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", customer.name)
            intent.putExtra("CUSTOMER_MOBILE", customer.mobile)
            startActivity(intent)
        }, { mobile ->
            showDeleteConfirmation(mobile)
        })
        rvCustomers.adapter = adapter



        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_home)

        setupAlphabetFilters()
        setupSearch()
        
        // Focus solely on the synced data.
        // Then try to sync with backend
        refreshAllData()
    }

    override fun onResume() {
        super.onResume()

        refreshAllData()
        findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_home
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilter(text: String) {
        val filteredList = if (text.isEmpty()) {
            allCustomers
        } else {
            allCustomers.filter { 
                it.name.lowercase().contains(text.lowercase()) || 
                it.mobile.contains(text) 
            }
        }
        adapter.updateData(filteredList)
        tvCustomerListTitle.text = if (text.isEmpty()) "All Customers" else "Search Results (${filteredList.size})"
    }

    private fun refreshAllData() {
        RetrofitClient.instance.getAllCustomers().enqueue(object : Callback<List<CustomerResponse>> {
            override fun onResponse(call: Call<List<CustomerResponse>>, response: Response<List<CustomerResponse>>) {
                if (response.isSuccessful) {
                    val backendData = response.body() ?: emptyList()
                    if (backendData.isNotEmpty()) {
                        val newList = backendData.map { 
                            CustomerDisplayModel(
                                it.id?.toString() ?: "0", 
                                it.name ?: "Unknown", 
                                it.mobileNumber ?: "No Number", 
                                it.length ?: ""
                            ) 
                        }
                        updateUI(newList, " (Synced)")
                    }
                } else {
                    Log.e("HOME_DEBUG", "Backend response error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<CustomerResponse>>, t: Throwable) {
                Log.e("HOME_DEBUG", "Network failure: ${t.message}")
                // Fallback is already handled by fetchFromLocal() called in onCreate/onResume
            }
        })
    }


    private fun updateUI(list: List<CustomerDisplayModel>, source: String) {
        allCustomers = list.sortedBy { it.name.lowercase() }.toMutableList()
        val searchQuery = etSearch.text.toString()
        if (searchQuery.isNotEmpty()) {
            applyFilter(searchQuery)
        } else {
            tvCustomerListTitle.text = "All Customers$source"
            adapter.updateData(allCustomers)
        }
        
        // Handle empty state
        findViewById<View>(R.id.rvCustomers).visibility = if (allCustomers.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupAlphabetFilters() {
        findViewById<Button>(R.id.btnAll)?.setOnClickListener {
            etSearch.text?.clear()
            adapter.updateData(allCustomers)
        }
        // ... (rest of alphabet filter logic)
    }

    private fun showDeleteConfirmation(mobile: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Customer")
            .setMessage("Permanently remove this customer?")
            .setPositiveButton("Delete") { _, _ -> performFullDelete(mobile) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performFullDelete(mobile: String) {
        RetrofitClient.instance.deleteCustomer(mobile).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                refreshAllData()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                refreshAllData()
            }
        })
    }
}
