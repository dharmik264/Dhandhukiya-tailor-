package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCustomerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_customer)

        val etCustomerName = findViewById<EditText>(R.id.etCustomerName) ?: return
        val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber) ?: return
        val etAddress = findViewById<EditText>(R.id.etAddress) ?: return
        val btnSaveCustomer = findViewById<Button>(R.id.btnSaveCustomer) ?: return
        val tvTopTitle = findViewById<android.widget.TextView>(R.id.tvTopTitle) ?: return

        val isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        if (isEditMode) {
            tvTopTitle.text = "Edit Customer"
            val existingName = intent.getStringExtra("CUSTOMER_NAME") ?: ""
            val existingMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
            etCustomerName.setText(existingName)
            etMobileNumber.setText(existingMobile)
            
            // Fecth address from local DB instead of backend
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@AddCustomerActivity)
                val customer = withContext(Dispatchers.IO) {
                    db.customerDao().getAll().find { it.mobileNumber == existingMobile }
                }
                etAddress.setText(customer?.address ?: "")
            }
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnBackCard)?.setOnClickListener {
            animateButtonClick(it)
            it.postDelayed({ onBackPressedDispatcher.onBackPressed() }, 150)
        }

        btnSaveCustomer.setOnClickListener {
            animateButtonClick(it)
            val name = etCustomerName.text.toString().trim()
            val mobile = etMobileNumber.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Name and Mobile are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
                Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSaveCustomer.isEnabled = false

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@AddCustomerActivity)
                val existingCustomer = withContext(Dispatchers.IO) {
                    db.customerDao().getAll().find { it.mobileNumber == mobile }
                }

                if (existingCustomer != null && !isEditMode) {
                    btnSaveCustomer.isEnabled = true
                    androidx.appcompat.app.AlertDialog.Builder(this@AddCustomerActivity)
                        .setTitle("Customer Already Exists")
                        .setMessage("A customer with this mobile number ($mobile) already exists. Do you want to update their details instead?")
                        .setPositiveButton("Update") { _, _ -> 
                            performSave(name, mobile, address)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    performSave(name, mobile, address)
                }
            }
        }

        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_customers)
    }

    private fun performSave(name: String, mobile: String, address: String) {
        val btnSaveCustomer = findViewById<Button>(R.id.btnSaveCustomer)
        val isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@AddCustomerActivity)
                val customerDao = db.customerDao()
                
                val existingCustomer = withContext(Dispatchers.IO) {
                    customerDao.getAll().find { it.mobileNumber == mobile }
                }

                val newCustomer = Customer(
                    id = existingCustomer?.id ?: 0,
                    firstName = name.split(" ").firstOrNull() ?: "",
                    lastName = name.split(" ").drop(1).joinToString(" "),
                    name = name,
                    mobileNumber = mobile,
                    address = address,
                    isSynced = true, // Force true to avoid any sync attempts
                    updatedAt = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    if (existingCustomer != null || isEditMode) {
                        customerDao.update(newCustomer)
                    } else {
                        customerDao.insert(newCustomer)
                    }
                }

                Toast.makeText(this@AddCustomerActivity, "Customer Saved!", Toast.LENGTH_SHORT).show()
                navigateToMeasurements(name, mobile, -1, isEditMode)
                
            } catch (e: Exception) {
                Log.e("ROOM_SAVE", "Error saving customer: ${e.message}")
                Toast.makeText(this@AddCustomerActivity, "Error saving to database", Toast.LENGTH_SHORT).show()
                btnSaveCustomer?.isEnabled = true
            }
        }
    }

    private fun animateButtonClick(view: View) {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
        view.animate().cancel()
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(120)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .setInterpolator(android.view.animation.AccelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun navigateToMeasurements(name: String, mobile: String, id: Int, isEdit: Boolean) {
        val intent = Intent(this@AddCustomerActivity, AddMeasurementsActivity::class.java)
        intent.putExtra("CUSTOMER_NAME", name)
        intent.putExtra("CUSTOMER_MOBILE", mobile)
        intent.putExtra("CUSTOMER_ID", id)
        intent.putExtra("IS_EDIT_MODE", isEdit)
        startActivity(intent)
        finish()
    }
}