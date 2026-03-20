package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCustomerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_customer, container, false)

        // Fragments shouldn't show the internal navbar as MainActivity provides one
        view.findViewById<View>(R.id.localBottomNavigation)?.visibility = View.GONE

        val etCustomerName = view.findViewById<EditText>(R.id.etCustomerName)
        val etMobileNumber = view.findViewById<EditText>(R.id.etMobileNumber)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val btnSaveCustomer = view.findViewById<Button>(R.id.btnSaveCustomer)
        
        // Back button to return to home tab
        view.findViewById<ImageView>(R.id.btnBack)?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                (activity as? MainActivity)?.viewPager?.currentItem = 0
            }
        }

        btnSaveCustomer.setOnClickListener {
            val name = etCustomerName.text.toString().trim()
            val mobile = etMobileNumber.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || mobile.isEmpty()) {
                context?.let { Toast.makeText(it, "Name and Mobile are required", Toast.LENGTH_SHORT).show() }
                return@setOnClickListener
            }

            btnSaveCustomer.isEnabled = false

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(requireContext())
                    val customerDao = db.customerDao()
                    
                    val existing = customerDao.getAll().find { it.mobileNumber == mobile }
                    if (existing != null) {
                        withContext(Dispatchers.Main) {
                            if (isAdded) {
                                Toast.makeText(context, "Customer with this number already exists!", Toast.LENGTH_LONG).show()
                                btnSaveCustomer.isEnabled = true
                            }
                        }
                        return@launch
                    }
                    
                    val newCustomer = Customer(
                        name = name,
                        mobileNumber = mobile,
                        address = address,
                        isSynced = true, // Set to true for offline mode
                        updatedAt = System.currentTimeMillis()
                    )

                    customerDao.insert(newCustomer)

                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            Toast.makeText(context, "Customer Saved!", Toast.LENGTH_SHORT).show()
                            navigateToMeasurements(name, mobile)
                            btnSaveCustomer.isEnabled = true
                            etCustomerName.text.clear()
                            etMobileNumber.text.clear()
                            etAddress.text.clear()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            btnSaveCustomer.isEnabled = true
                            Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun navigateToMeasurements(name: String, mobile: String) {
        context?.let { ctx ->
            val intent = Intent(ctx, AddMeasurementsActivity::class.java).apply {
                putExtra("CUSTOMER_NAME", name)
                putExtra("CUSTOMER_MOBILE", mobile)
            }
            startActivity(intent)
        }
    }
}
