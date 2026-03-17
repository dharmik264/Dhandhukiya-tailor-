package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMeasurementsActivity : AppCompatActivity() {

    private var btnShirt: MaterialButton? = null
    private var btnPant: MaterialButton? = null
    private var btnKoti: MaterialButton? = null
    private var btnSuit: MaterialButton? = null
    private var btnJabbho: MaterialButton? = null
    private var btnLehngho: MaterialButton? = null
    private var btnSafari: MaterialButton? = null
    private var btnJodhpuri: MaterialButton? = null
    private var tvMeasurementTitle: TextView? = null
    private var tvCustomerName: TextView? = null
    
    private var etLength: EditText? = null
    private var etChest: EditText? = null
    private var etWaist: EditText? = null
    private var etCollar: EditText? = null
    private var etShoulder: EditText? = null
    private var etSleeve: EditText? = null
    private var etHip: EditText? = null
    private var etRise: EditText? = null
    private var etNotes: EditText? = null

    private var tilLength: TextInputLayout? = null
    private var tilChest: TextInputLayout? = null
    private var tilWaist: TextInputLayout? = null
    private var tilCollar: TextInputLayout? = null
    private var tilShoulder: TextInputLayout? = null
    private var tilSleeve: TextInputLayout? = null
    private var tilHip: TextInputLayout? = null
    private var tilRise: TextInputLayout? = null

    private var btnSaveMeasurements: Button? = null
    
    private var selectedGarment = "Shirt"
    private var customerMobile = ""
    private var customerId: Int = -1
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_measurements)

        initViews()
        setupIntentData()
        setupListeners()
        
        // Initial State
        updateMeasurementUI(selectedGarment)
    }

    private fun initViews() {
        btnShirt = findViewById(R.id.btnShirtProfile)
        btnPant = findViewById(R.id.btnPantProfile)
        btnKoti = findViewById(R.id.btnKotiProfile)
        btnSuit = findViewById(R.id.btnSuitProfile)
        btnJabbho = findViewById(R.id.btnJabbhoProfile)
        btnLehngho = findViewById(R.id.btnLehnghoProfile)
        btnSafari = findViewById(R.id.btnSafariProfile)
        btnJodhpuri = findViewById(R.id.btnJodhpuriProfile)
        
        tvMeasurementTitle = findViewById(R.id.tvMeasurementTitle)
        tvCustomerName = findViewById(R.id.tvCustomerName)

        etLength = findViewById(R.id.etLength)
        etChest = findViewById(R.id.etChest)
        etWaist = findViewById(R.id.etWaist)
        etCollar = findViewById(R.id.etCollar)
        etShoulder = findViewById(R.id.etShoulder)
        etSleeve = findViewById(R.id.etSleeve)
        etHip = findViewById(R.id.etHip)
        etRise = findViewById(R.id.etRise)
        etNotes = findViewById(R.id.etNotes)

        tilLength = findViewById(R.id.tilLength)
        tilChest = findViewById(R.id.tilChest)
        tilWaist = findViewById(R.id.tilWaist)
        tilCollar = findViewById(R.id.tilCollar)
        tilShoulder = findViewById(R.id.tilShoulder)
        tilSleeve = findViewById(R.id.tilSleeve)
        tilHip = findViewById(R.id.tilHip)
        tilRise = findViewById(R.id.tilRise)

        btnSaveMeasurements = findViewById(R.id.btnSaveMeasurements)
    }

    private fun setupIntentData() {
        val name = intent.getStringExtra("CUSTOMER_NAME") ?: "Customer"
        customerMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
        customerId = intent.getIntExtra("CUSTOMER_ID", -1)
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        tvCustomerName?.text = name
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        val buttons = listOfNotNull(btnShirt, btnPant, btnKoti, btnSuit, btnJabbho, btnLehngho, btnSafari, btnJodhpuri)
        buttons.forEach { button ->
            button.setOnClickListener {
                val type = button.text.toString()
                if (selectedGarment != type) {
                    selectedGarment = type
                    updateMeasurementUI(type)
                }
            }
        }

        btnSaveMeasurements?.setOnClickListener { saveAndSyncData() }
        
        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_customers)
    }

    private fun clearAllFields() {
        val fields = listOf(etLength, etChest, etWaist, etCollar, etShoulder, etSleeve, etHip, etRise, etNotes)
        fields.forEach { it?.setText("") }
    }

    private fun updateMeasurementUI(garmentType: String) {
        tvMeasurementTitle?.text = "$garmentType Measurements"
        
        // 1. Reset field visibility
        val allTils = listOfNotNull(tilLength, tilChest, tilWaist, tilCollar, tilShoulder, tilSleeve, tilHip, tilRise)
        allTils.forEach { it.visibility = View.VISIBLE }
        
        // 2. Adjust per garment type
        when (garmentType) {
            "Pant", "Lehngho" -> {
                tilLength?.hint = "Outseam"
                tilWaist?.hint = "Waist"
                tilCollar?.hint = "Inseam"
                tilHip?.hint = "Hip"
                tilRise?.hint = "Rise"
                
                tilChest?.visibility = View.GONE
                tilShoulder?.visibility = View.GONE
                tilSleeve?.visibility = View.GONE
            }
            "Koti" -> {
                tilLength?.hint = "Length"
                tilChest?.hint = "Chest"
                tilWaist?.hint = "Waist"
                tilShoulder?.hint = "Shoulder"
                tilSleeve?.hint = "Armhole" // Use Sleeve field for Armhole in Koti
                tilCollar?.hint = "Neck"
                
                tilHip?.visibility = View.GONE
                tilRise?.visibility = View.GONE
            }
            "Shirt", "Jabbho" -> {
                tilLength?.hint = "Length"; tilChest?.hint = "Chest"
                tilWaist?.hint = "Waist"; tilCollar?.hint = "Collar"
                tilShoulder?.hint = "Shoulder"; tilSleeve?.hint = "Sleeve"
                
                if (garmentType == "Jabbho") tilWaist?.visibility = View.GONE
                tilHip?.visibility = View.GONE
                tilRise?.visibility = View.GONE
            }
            "Suit", "Safari", "Jodhpuri" -> {
                tilLength?.hint = "Length"; tilChest?.hint = "Chest"
                tilWaist?.hint = "Waist"; tilCollar?.hint = "Collar"
                tilShoulder?.hint = "Shoulder"; tilSleeve?.hint = "Sleeve"
                tilHip?.hint = "Hip"; tilRise?.hint = "Rise"
                
                if (garmentType == "Suit") {
                    tilHip?.visibility = View.GONE
                    tilRise?.visibility = View.GONE
                } else if (garmentType == "Jodhpuri") {
                    tilRise?.visibility = View.GONE
                }
            }
        }
        
        // 3. Update Button Styles
        val buttons = listOfNotNull(btnShirt, btnPant, btnKoti, btnSuit, btnJabbho, btnLehngho, btnSafari, btnJodhpuri)
        buttons.forEach { btn ->
            val isSelected = btn.text.toString() == garmentType
            btn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, if (isSelected) R.color.red_dark else R.color.white)
            )
            btn.setTextColor(if (isSelected) Color.WHITE else ContextCompat.getColor(this, R.color.on_surface))
        }

        // 4. Load existing data if edit mode
        clearAllFields()
        if (isEditMode) {
            fetchMeasurementsForEdit(garmentType)
        }
    }

    private fun fetchMeasurementsForEdit(type: String) {
        if (customerMobile.isEmpty()) return
        
        RetrofitClient.instance.getCustomerMeasurements(customerMobile, type).enqueue(object : Callback<MeasurementResponse> {
            override fun onResponse(call: Call<MeasurementResponse>, response: Response<MeasurementResponse>) {
                if (response.isSuccessful) {
                    val m = response.body()
                    if (m != null && m.id != null) {
                        etLength?.setText(m.length ?: "")
                        etChest?.setText(m.chest ?: "")
                        etWaist?.setText(m.waist ?: "")
                        etCollar?.setText(m.collar ?: "")
                        etShoulder?.setText(m.shoulder ?: "")
                        etSleeve?.setText(m.sleeve ?: "")
                        etHip?.setText(m.hip ?: "")
                        etRise?.setText(m.rise ?: "")
                        etNotes?.setText(m.notes ?: "")
                    }
                }
            }
            override fun onFailure(call: Call<MeasurementResponse>, t: Throwable) {
                Log.e("ADD_MEAS_EDIT", "Failed to fetch: ${t.message}")
            }
        })
    }

    private fun saveAndSyncData() {
        val l = etLength?.text?.toString()?.trim() ?: ""
        val c = etChest?.text?.toString()?.trim() ?: ""
        val w = etWaist?.text?.toString()?.trim() ?: ""
        val cl = etCollar?.text?.toString()?.trim() ?: ""
        val sh = etShoulder?.text?.toString()?.trim() ?: ""
        val sl = etSleeve?.text?.toString()?.trim() ?: ""
        val h = etHip?.text?.toString()?.trim() ?: ""
        val r = etRise?.text?.toString()?.trim() ?: ""
        val n = etNotes?.text?.toString()?.trim() ?: ""

        if (l.isEmpty() && c.isEmpty() && w.isEmpty() && cl.isEmpty() && sh.isEmpty() && sl.isEmpty() && h.isEmpty() && r.isEmpty()) {
            Toast.makeText(this, "Please enter at least one measurement", Toast.LENGTH_SHORT).show()
            return
        }

        btnSaveMeasurements?.isEnabled = false
        
        val uid = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("USER_ID", 1).toString()
        val data = mapOf(
            "user_id" to uid,
            "mobile_number" to customerMobile,
            "garment_type" to selectedGarment,
            "length" to l, "chest" to c, "waist" to w,
            "collar" to cl, "shoulder" to sh, "sleeve" to sl,
            "hip" to h, "rise" to r, "notes" to n,
            "status" to "Pending", "is_update" to isEditMode.toString()
        )

        RetrofitClient.instance.addMeasurement(data).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                btnSaveMeasurements?.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@AddMeasurementsActivity, "Measurements Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddMeasurementsActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnSaveMeasurements?.isEnabled = true
                Toast.makeText(this@AddMeasurementsActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}