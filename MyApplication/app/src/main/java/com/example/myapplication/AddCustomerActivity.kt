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
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCustomerActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_customer)


        val etCustomerName = findViewById<EditText>(R.id.etCustomerName)
        val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val btnSaveCustomer = findViewById<Button>(R.id.btnSaveCustomer)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvTopTitle = findViewById<android.widget.TextView>(R.id.tvTopTitle)

        val isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        if (isEditMode) {
            tvTopTitle?.text = "Edit Customer"
            val existingName = intent.getStringExtra("CUSTOMER_NAME") ?: ""
            val existingMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
            etCustomerName.setText(existingName)
            etMobileNumber.setText(existingMobile)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnBackCard).setOnClickListener {
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

            btnSaveCustomer.isEnabled = false

            val customerData = mapOf(
                "name" to name,
                "mobile_number" to mobile,
                "address" to address
            )

            RetrofitClient.instance.addCustomer(customerData)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        btnSaveCustomer.isEnabled = true
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddCustomerActivity, "Customer Saved!", Toast.LENGTH_SHORT).show()
                            navigateToMeasurements(name, mobile, -1)
                        } else {
                            Toast.makeText(this@AddCustomerActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        btnSaveCustomer.isEnabled = true
                        Toast.makeText(this@AddCustomerActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_customers)
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

    private fun navigateToMeasurements(name: String, mobile: String, id: Int) {
        val intent = Intent(this@AddCustomerActivity, AddMeasurementsActivity::class.java)
        intent.putExtra("CUSTOMER_NAME", name)
        intent.putExtra("CUSTOMER_MOBILE", mobile)
        intent.putExtra("CUSTOMER_ID", id) // Pass the actual ID from DB
        startActivity(intent)
        finish()
    }
}