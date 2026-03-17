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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

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
        ResponsiveUtils.setupResponsiveRecyclerView(currentContext, rvCustomers, 300) // 300dp per customer item in grid
        adapter = CustomerAdapter(emptyList(), { customer ->
            context?.let { ctx ->
                val intent = Intent(ctx, CustomerProfileActivity::class.java)
                intent.putExtra("CUSTOMER_NAME", customer.name)
                intent.putExtra("CUSTOMER_MOBILE", customer.mobile)
                intent.putExtra("SELECTED_GARMENT", "Shirt") // Default to Shirt for now
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
                            length = it.length ?: "",
                            status = it.status ?: "No Orders"
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
        val request = Request.Builder()
            .url("https://api.github.com/repos/dharmik264/Dhandhukiya-tailor-/releases")
            .build()
        
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("AppVersion", "Failed to check app version: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) return
                val responseData = response.body?.string() ?: return
                
                try {
                    val jsonArray = org.json.JSONArray(responseData)
                    if (jsonArray.length() == 0) return
                    
                    val json = jsonArray.getJSONObject(0)
                    val latestTag = json.getString("tag_name")
                    val latestVersion = latestTag.removePrefix("v") 
                    
                    Log.d("AppUpdate", "Latest Tag: $latestTag, latestVersion: $latestVersion")

                    val assets = json.getJSONArray("assets")
                    var apkUrl: String? = null
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.getString("name").endsWith(".apk")) {
                            apkUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                    
                    activity?.runOnUiThread {
                        if (!isAdded) return@runOnUiThread
                        val currentVersion = try {
                            val info = context?.packageManager?.getPackageInfo(context?.packageName ?: "", 0)
                            Log.d("AppUpdate", "Current Version from Package: ${info?.versionName}")
                            info?.versionName ?: "1.0"
                        } catch (e: Exception) {
                            "1.0"
                        }
                        
                        val prefs = context?.getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                        val dismissedVersion = prefs?.getString("DISMISSED_VERSION", "") ?: ""
                        
                        // Smarter version check
                        val isUpdateAvailable = try {
                            val cParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
                            val lParts = latestVersion.split(".").mapNotNull { it.toIntOrNull() }
                            var newer = latestVersion != currentVersion && lParts.isNotEmpty()
                            if (lParts.isNotEmpty() && cParts.isNotEmpty()) {
                                for (i in 0 until maxOf(cParts.size, lParts.size)) {
                                    val c = cParts.getOrElse(i) { 0 }
                                    val l = lParts.getOrElse(i) { 0 }
                                    if (l > c) { newer = true; break }
                                    if (c > l) { newer = false; break }
                                }
                            }
                            newer
                        } catch (e: Exception) {
                            latestVersion != currentVersion
                        }
                        
                        if (isUpdateAvailable && dismissedVersion != latestVersion) {
                            showUpdateDialog(apkUrl, latestVersion, false)
                        } else if (!isUpdateAvailable && dismissedVersion.isNotEmpty()) {
                            prefs?.edit()?.remove("DISMISSED_VERSION")?.apply()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AppVersion", "Error parsing release info: ${e.message}")
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
