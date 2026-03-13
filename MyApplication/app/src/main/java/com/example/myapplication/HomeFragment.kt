package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        
        // Hide the navigation bar because it's now in the activity
        view.findViewById<View>(R.id.bottomNavigation)?.visibility = View.GONE

        tvCustomerListTitle = view.findViewById(R.id.tvCustomerListTitle)
        rvCustomers = view.findViewById(R.id.rvCustomers)
        etSearch = view.findViewById(R.id.etSearch)

        val currentContext = context ?: return view
        rvCustomers.layoutManager = LinearLayoutManager(currentContext)
        adapter = CustomerAdapter(emptyList(), { customer ->
            context?.let { ctx ->
                val intent = Intent(ctx, CustomerProfileActivity::class.java)
                intent.putExtra("CUSTOMER_NAME", customer.name)
                intent.putExtra("CUSTOMER_MOBILE", customer.mobile)
                startActivity(intent)
            }
        }, { mobile ->
            if (isAdded) showDeleteConfirmation(mobile)
        })
        rvCustomers.adapter = adapter

        setupAlphabetFilters(view)
        setupSearch()
        refreshAllData()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshAllData()
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
                if (isAdded && response.isSuccessful) {
                    val backendData = response.body() ?: emptyList()
                    val newList = backendData.map { 
                        CustomerDisplayModel(
                            id = it.id?.toString() ?: "0", 
                            name = it.name ?: "Unknown", 
                            mobile = it.mobileNumber ?: "No Number", 
                            length = it.length ?: ""
                        ) 
                    }
                    updateUI(newList, " (Synced)")
                }
            }

            override fun onFailure(call: Call<List<CustomerResponse>>, t: Throwable) {
                Log.e("HOME_FRAG", "Network failure: ${t.message}")
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
        rvCustomers.visibility = if (allCustomers.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupAlphabetFilters(view: View) {
        view.findViewById<Button>(R.id.btnAll)?.setOnClickListener {
            etSearch.text?.clear()
            adapter.updateData(allCustomers)
        }
        val currentContext = context ?: return
        val alphabets = 'A'..'Z'
        alphabets.forEach { char ->
            val resId = resources.getIdentifier("btn$char", "id", currentContext.packageName)
            if (resId != 0) {
                view.findViewById<Button>(resId)?.setOnClickListener {
                    val filtered = allCustomers.filter { it.name.startsWith(char, ignoreCase = true) }
                    adapter.updateData(filtered)
                    tvCustomerListTitle.text = "Starting with $char (${filtered.size})"
                }
            }
        }
    }

    private fun showDeleteConfirmation(mobile: String) {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle("Delete Customer")
            .setMessage("Permanently remove this customer?")
            .setPositiveButton("Delete") { _, _ -> performFullDelete(mobile) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performFullDelete(mobile: String) {
        RetrofitClient.instance.deleteCustomer(mobile).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (isAdded) refreshAllData()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (isAdded) refreshAllData()
            }
        })
    }
}
