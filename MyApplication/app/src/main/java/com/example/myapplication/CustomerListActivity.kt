package com.example.myapplication

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomerListActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)


        displayCustomersData()
    }

    private fun displayCustomersData() {
        Toast.makeText(this, "Fetching live customer data from backend...", Toast.LENGTH_SHORT).show()
        // Here you would implement your Retrofit call or pass it via intent
    }
}