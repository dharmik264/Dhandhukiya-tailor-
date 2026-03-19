package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var tvCustomerListTitle: TextView
    private lateinit var rvCustomers: RecyclerView
    private lateinit var adapter: CustomerAdapter
    private lateinit var etSearch: TextInputEditText
    private var allCustomers = mutableListOf<CustomerDisplayModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_home, container, false)

        // Fragments shouldn't show the internal navbar as MainActivity provides one
        view.findViewById<View>(R.id.mainBottomNavigation)?.visibility = View.GONE
        
        tvCustomerListTitle = view.findViewById(R.id.tvCustomerListTitle)
        rvCustomers = view.findViewById(R.id.rvCustomers)
        etSearch = view.findViewById(R.id.etSearch)

        val currentContext = context ?: return view
        ResponsiveUtils.setupResponsiveRecyclerView(currentContext, rvCustomers, 300) 
        
        adapter = CustomerAdapter(emptyList(), { customer ->
            context?.let { ctx ->
                val intent = Intent(ctx, CustomerProfileActivity::class.java).apply {
                    putExtra("CUSTOMER_NAME", customer.name)
                    putExtra("CUSTOMER_MOBILE", customer.mobileNumber)
                }
                startActivity(intent)
            }
        }, { customer ->
            context?.let { ctx ->
                val intent = Intent(ctx, AddCustomerActivity::class.java).apply {
                    putExtra("CUSTOMER_NAME", customer.name)
                    putExtra("CUSTOMER_MOBILE", customer.mobileNumber)
                    putExtra("IS_EDIT_MODE", true)
                }
                startActivity(intent)
            }
        }, { mobileNumber ->
            if (isAdded) showDeleteConfirmation(mobileNumber)
        })
        rvCustomers.adapter = adapter

        setupAlphabetFilters(view)
        setupSearch()
        refreshData()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshData()
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
        val currentContext = context ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(currentContext)
                val localCustomers = db.customerDao().getAll()
                
                val displayList = localCustomers.map { 
                    CustomerDisplayModel(
                        id = it.id.toString(),
                        name = it.name,
                        mobileNumber = it.mobileNumber,
                        length = it.length,
                        status = it.status
                    )
                }

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        allCustomers = displayList.sortedBy { it.name.lowercase() }.toMutableList()
                        val searchQuery = etSearch.text.toString()
                        if (searchQuery.isNotEmpty()) {
                            applyFilter(searchQuery)
                        } else {
                            tvCustomerListTitle.text = "All Customers"
                            adapter.updateData(allCustomers)
                        }
                        rvCustomers.visibility = if (allCustomers.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("HOME_FRAG", "Data refresh error: ${e.message}")
            }
        }
    }

    private fun setupAlphabetFilters(view: View) {
        val layoutAlphabetBar = view.findViewById<ViewGroup>(R.id.layoutAlphabetBar) ?: return
        
        view.findViewById<View>(R.id.btnAll)?.setOnClickListener {
            etSearch.text?.clear()
            adapter.updateData(allCustomers)
            tvCustomerListTitle.text = "All Customers"
        }

        val alphabetList = ('A'..'Z').toList()
        val currentContext = context ?: return

        alphabetList.forEach { char ->
            val textView = TextView(currentContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (36 * resources.displayMetrics.density).toInt(),
                    (36 * resources.displayMetrics.density).toInt()
                ).apply {
                    marginEnd = (8 * resources.displayMetrics.density).toInt()
                }
                text = char.toString()
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setTextColor(resources.getColor(R.color.on_surface_variant, null))
                
                setOnClickListener {
                    val filtered = allCustomers.filter { it.name.startsWith(char, ignoreCase = true) }
                    adapter.updateData(filtered)
                    tvCustomerListTitle.text = "Starting with $char (${filtered.size})"
                    
                    // Highlight selected
                    for (i in 0 until layoutAlphabetBar.childCount) {
                        val child = layoutAlphabetBar.getChildAt(i) as? TextView
                        child?.setTextColor(resources.getColor(R.color.on_surface_variant, null))
                        child?.setBackgroundResource(0)
                    }
                    setTextColor(resources.getColor(android.R.color.white, null))
                    setBackgroundResource(R.drawable.bg_circle_red)
                }
            }
            layoutAlphabetBar.addView(textView)
        }
    }

    private fun showDeleteConfirmation(mobile: String) {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle("Delete Customer")
            .setMessage("Permanently remove this customer from your device?")
            .setPositiveButton("Delete") { _, _ -> 
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getDatabase(ctx)
                        val customer = db.customerDao().getAll().find { it.mobileNumber == mobile }
                        if (customer != null) {
                            db.customerDao().delete(customer)
                        }
                        withContext(Dispatchers.Main) {
                            if (isAdded) refreshData()
                        }
                    } catch (e: Exception) {
                        Log.e("DELETE_DEBUG", "Error: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
