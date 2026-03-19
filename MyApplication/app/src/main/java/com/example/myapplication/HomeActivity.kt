package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val intent = Intent(this, CustomerProfileActivity::class.java).apply {
                putExtra("CUSTOMER_NAME", customer.name)
                putExtra("CUSTOMER_MOBILE", customer.mobileNumber)
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

        val navBar = findViewById<BottomNavigationView>(R.id.mainBottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_home)

        setupAlphabetFilters()
        setupSearch()
        
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.selectedItemId = R.id.nav_home
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
                it.mobileNumber.contains(text) 
            }
        }
        adapter.updateData(filteredList)
        tvCustomerListTitle.text = if (text.isEmpty()) "All Customers" else "Search Results (${filteredList.size})"
    }

    private fun refreshData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@HomeActivity)
                val localList = db.customerDao().getAll().map {
                    CustomerDisplayModel(it.id.toString(), it.name, it.mobileNumber, it.length)
                }
                withContext(Dispatchers.Main) {
                    allCustomers = localList.sortedBy { it.name.lowercase() }.toMutableList()
                    val searchQuery = etSearch.text.toString()
                    if (searchQuery.isNotEmpty()) {
                        applyFilter(searchQuery)
                    } else {
                        tvCustomerListTitle.text = "All Customers"
                        adapter.updateData(allCustomers)
                    }
                    findViewById<View>(R.id.rvCustomers).visibility = if (allCustomers.isEmpty()) View.GONE else View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("HOME_DEBUG", "Local refresh failed: ${e.message}")
            }
        }
    }

    private fun setupAlphabetFilters() {
        val layoutAlphabetBar = findViewById<ViewGroup>(R.id.layoutAlphabetBar) ?: return
        
        findViewById<View>(R.id.btnAll)?.setOnClickListener {
            etSearch.text?.clear()
            adapter.updateData(allCustomers)
            tvCustomerListTitle.text = "All Customers"
        }

        val alphabetList = ('A'..'Z').toList()

        alphabetList.forEach { char ->
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (36 * resources.displayMetrics.density).toInt(),
                    (36 * resources.displayMetrics.density).toInt()
                ).apply {
                    marginEnd = (8 * resources.displayMetrics.density).toInt()
                }
                text = char.toString()
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.on_surface_variant))
                
                setOnClickListener {
                    val filtered = allCustomers.filter { it.name.startsWith(char, ignoreCase = true) }
                    adapter.updateData(filtered)
                    tvCustomerListTitle.text = "Starting with $char (${filtered.size})"
                    
                    // Highlight selected
                    for (i in 0 until layoutAlphabetBar.childCount) {
                        val child = layoutAlphabetBar.getChildAt(i) as? TextView
                        child?.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.on_surface_variant))
                        child?.setBackgroundResource(0)
                    }
                    setTextColor(ContextCompat.getColor(this@HomeActivity, android.R.color.white))
                    setBackgroundResource(R.drawable.bg_circle_red)
                }
            }
            layoutAlphabetBar.addView(textView)
        }
    }

    private fun showDeleteConfirmation(mobile: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Customer")
            .setMessage("Permanently remove this customer from your device?")
            .setPositiveButton("Delete") { _, _ -> 
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getDatabase(this@HomeActivity)
                        val customer = db.customerDao().getAll().find { it.mobileNumber == mobile }
                        if (customer != null) {
                            db.customerDao().delete(customer)
                        }
                        withContext(Dispatchers.Main) {
                            refreshData()
                        }
                    } catch (e: Exception) {
                        Log.e("DELETE_DEBUG", "Error deleting: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
