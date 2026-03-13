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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCustomerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_customer, container, false)

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

            val customerData = mapOf(
                "name" to name,
                "mobile_number" to mobile,
                "address" to address
            )

            RetrofitClient.instance.addCustomer(customerData)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (!isAdded) return
                        btnSaveCustomer.isEnabled = true
                        if (response.isSuccessful) {
                            context?.let { Toast.makeText(it, "Customer Saved!", Toast.LENGTH_SHORT).show() }
                            navigateToMeasurements(name, mobile, -1)
                        } else {
                            context?.let { Toast.makeText(it, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show() }
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        if (!isAdded) return
                        btnSaveCustomer.isEnabled = true
                        context?.let { Toast.makeText(it, "Network Error", Toast.LENGTH_SHORT).show() }
                    }
                })
        }

        return view
    }

    private fun navigateToMeasurements(name: String, mobile: String, id: Int) {
        context?.let { ctx ->
            val intent = Intent(ctx, AddMeasurementsActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", name)
            intent.putExtra("CUSTOMER_MOBILE", mobile)
            intent.putExtra("CUSTOMER_ID", id)
            startActivity(intent)
        }
    }
}
