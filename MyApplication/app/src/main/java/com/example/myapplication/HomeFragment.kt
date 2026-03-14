package com.example.myapplication

import android.content.Intent
import android.net.Uri
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
        }, { customer ->
            context?.let { ctx ->
                val intent = Intent(ctx, AddCustomerActivity::class.java)
                intent.putExtra("CUSTOMER_NAME", customer.name)
                intent.putExtra("CUSTOMER_MOBILE", customer.mobile)
                intent.putExtra("IS_EDIT_MODE", true)
                startActivity(intent)
            }
        }, { mobile ->
            if (isAdded) showDeleteConfirmation(mobile)
        })
        rvCustomers.adapter = adapter

        setupAlphabetFilters(view)
        setupSearch()
        refreshAllData()
        checkAppVersion()

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

    private fun checkAppVersion() {
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("https://api.github.com/repos/dharmik264/Dhandhukiya-tailor-/releases/latest")
            .build()
        
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) return
                
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val json = org.json.JSONObject(responseData)
                        // Uses the GitHub release tag as the latest version identifier
                        val latestVersion = json.getString("tag_name").removePrefix("v") 
                        
                        val assets = json.getJSONArray("assets")
                        var apkUrl: String? = null
                        if (assets.length() > 0) {
                            apkUrl = assets.getJSONObject(0).getString("browser_download_url")
                        }
                        
                        activity?.runOnUiThread {
                            if (!isAdded) return@runOnUiThread
                            val currentVersion = try {
                                context?.packageManager?.getPackageInfo(context?.packageName ?: "", 0)?.versionName ?: "1.0"
                            } catch (e: Exception) {
                                "1.0"
                            }
                            
                            val prefs = context?.getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                            val dismissedVersion = prefs?.getString("DISMISSED_VERSION", "")
                            
                            if (currentVersion != latestVersion && dismissedVersion != latestVersion) {
                                showUpdateDialog(apkUrl, latestVersion, false)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun showUpdateDialog(apkUrl: String?, latestVersion: String, forceUpdate: Boolean) {
        val ctx = context ?: return
        val builder = AlertDialog.Builder(ctx)
            .setTitle("New Update Available")
            .setMessage("Please update the app to version $latestVersion to continue using all features.")
            .setPositiveButton("Update Now") { _, _ ->
                apkUrl?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    startActivity(intent)
                }
            }
            .setCancelable(false)

        if (!forceUpdate) {
            builder.setNegativeButton("Later") { _, _ ->
                val prefs = ctx.getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("DISMISSED_VERSION", latestVersion).apply()
            }
        }
        
        builder.show()
    }
}
