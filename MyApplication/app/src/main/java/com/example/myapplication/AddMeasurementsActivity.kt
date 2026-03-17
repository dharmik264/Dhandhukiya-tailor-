package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMeasurementsActivity : AppCompatActivity() {

    private lateinit var btnShirt: MaterialButton
    private lateinit var btnPant: MaterialButton
    private lateinit var btnKoti: MaterialButton
    private lateinit var btnSuit: MaterialButton
    private lateinit var btnJabbho: MaterialButton
    private lateinit var btnLehngho: MaterialButton
    private lateinit var btnSafari: MaterialButton
    private lateinit var btnJodhpuri: MaterialButton
    private lateinit var tvMeasurementTitle: TextView
    private lateinit var tvCustomerName: TextView
    
    private lateinit var etLength: EditText
    private lateinit var etChest: EditText
    private lateinit var etWaist: EditText
    private lateinit var etCollar: EditText
    private lateinit var etShoulder: EditText
    private lateinit var etSleeve: EditText
    private lateinit var etHip: EditText
    private lateinit var etRise: EditText
    private lateinit var etNotes: EditText

    private lateinit var tilLength: TextInputLayout
    private lateinit var tilChest: TextInputLayout
    private lateinit var tilWaist: TextInputLayout
    private lateinit var tilCollar: TextInputLayout
    private lateinit var tilShoulder: TextInputLayout
    private lateinit var tilSleeve: TextInputLayout
    private lateinit var tilHip: TextInputLayout
    private lateinit var tilRise: TextInputLayout

    private lateinit var btnSaveMeasurements: Button
    
    private var selectedGarment = "Shirt"
    private var customerMobile = ""
    private var customerId: Int = -1
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_measurements)

        // Initialize buttons
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

        // Initialize EditTexts
        etLength = findViewById(R.id.etLength)
        etChest = findViewById(R.id.etChest)
        etWaist = findViewById(R.id.etWaist)
        etCollar = findViewById(R.id.etCollar)
        etShoulder = findViewById(R.id.etShoulder)
        etSleeve = findViewById(R.id.etSleeve)
        etHip = findViewById(R.id.etHip)
        etRise = findViewById(R.id.etRise)
        etNotes = findViewById(R.id.etNotes)

        // Initialize TextInputLayouts
        tilLength = findViewById(R.id.tilLength)
        tilChest = findViewById(R.id.tilChest)
        tilWaist = findViewById(R.id.tilWaist)
        tilCollar = findViewById(R.id.tilCollar)
        tilShoulder = findViewById(R.id.tilShoulder)
        tilSleeve = findViewById(R.id.tilSleeve)
        tilHip = findViewById(R.id.tilHip)
        tilRise = findViewById(R.id.tilRise)

        btnSaveMeasurements = findViewById(R.id.btnSaveMeasurements)

        // Get info from intent
        val name = intent.getStringExtra("CUSTOMER_NAME") ?: "Customer"
        customerMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
        customerId = intent.getIntExtra("CUSTOMER_ID", -1)
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        tvCustomerName.text = name

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.btnDeleteMeasurement)?.setOnClickListener {
            Toast.makeText(this, "Delete feature coming soon", Toast.LENGTH_SHORT).show()
        }

        val buttons = listOf(btnShirt, btnPant, btnKoti, btnSuit, btnJabbho, btnLehngho, btnSafari, btnJodhpuri)
        buttons.forEach { button ->
            button.setOnClickListener {
                selectedGarment = button.text.toString()
                selectButton(button, buttons)
                updateMeasurementUI(selectedGarment)
                if (isEditMode) {
                    fetchMeasurementsForEdit(selectedGarment)
                }
            }
        }

        // Initial UI state
        updateMeasurementUI("Shirt")
        if (isEditMode) {
            fetchMeasurementsForEdit("Shirt")
        }

        btnSaveMeasurements.setOnClickListener {
            saveAndSyncData()
        }

        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_customers)
    }

    private fun fetchMeasurementsForEdit(type: String) {
        RetrofitClient.instance.getCustomerMeasurements(customerMobile, type).enqueue(object : Callback<MeasurementResponse> {
            override fun onResponse(call: Call<MeasurementResponse>, response: Response<MeasurementResponse>) {
                if (response.isSuccessful) {
                    val m = response.body()
                    if (m != null) {
                        etLength.setText(m.length)
                        etChest.setText(m.chest)
                        etWaist.setText(m.waist)
                        etCollar.setText(m.collar)
                        etShoulder.setText(m.shoulder)
                        etSleeve.setText(m.sleeve)
                        etHip.setText(m.hip)
                        etRise.setText(m.rise)
                        etNotes.setText(m.notes)
                    }
                }
            }
            override fun onFailure(call: Call<MeasurementResponse>, t: Throwable) {
                Log.e("EDIT_FETCH", "Failed to fetch for edit: ${t.message}")
            }
        })
    }

    private fun updateMeasurementUI(garmentType: String) {
        tvMeasurementTitle.text = "$garmentType Measurements"
        
        val allTils = listOf(tilLength, tilChest, tilWaist, tilCollar, tilShoulder, tilSleeve, tilHip, tilRise)
        allTils.forEach { it.visibility = View.VISIBLE }

        when (garmentType) {
            "Pant" -> {
                tilWaist.hint = "Waist (Natural Line)"
                tilHip.hint = "Hip (Widest Part)"
                tilCollar.hint = "Inseam (Crotch to Ankle)"
                tilLength.hint = "Outseam (Waist to Floor)"
                tilRise.hint = "Rise (Crotch Depth)"
                
                tilChest.visibility = View.GONE
                tilShoulder.visibility = View.GONE
                tilSleeve.visibility = View.GONE
            }
            "Koti" -> {
                tilChest.hint = "Chest (Fullest Part)"
                tilWaist.hint = "Waist / Stomach"
                tilShoulder.hint = "Shoulder (Across Back)"
                tilLength.hint = "Length of Koti"
                tilSleeve.hint = "Armhole"
                tilCollar.hint = "Neckline (Collar)"
                
                tilHip.visibility = View.GONE
                tilRise.visibility = View.GONE
            }
            "Suit" -> {
                tilChest.hint = "Chest"
                tilShoulder.hint = "Shoulder Width"
                tilSleeve.hint = "Sleeve Length"
                tilLength.hint = "Jacket Length"
                tilWaist.hint = "Waist"
                tilCollar.hint = "Inseam"
                
                tilHip.visibility = View.GONE
                tilRise.visibility = View.GONE
            }
            "Jabbho" -> {
                tilLength.hint = "Length"
                tilChest.hint = "Chest (Bust)"
                tilShoulder.hint = "Shoulder Width"
                tilSleeve.hint = "Sleeve Length"
                tilCollar.hint = "Neck Circumference"
                
                tilWaist.visibility = View.GONE
                tilHip.visibility = View.GONE
                tilRise.visibility = View.GONE
            }
            "Lehngho" -> {
                tilWaist.hint = "Waist"
                tilHip.hint = "Hip"
                tilCollar.hint = "Inseam"
                tilLength.hint = "Outseam"
                tilRise.hint = "Rise"
                
                tilChest.visibility = View.GONE
                tilShoulder.visibility = View.GONE
                tilSleeve.visibility = View.GONE
            }
            "Safari" -> {
                tilCollar.hint = "Neck"
                tilShoulder.hint = "Shoulder Width"
                tilChest.hint = "Chest"
                tilSleeve.hint = "Arm Length"
                tilWaist.hint = "Waist"
                tilHip.hint = "Hips / Seat"
                tilRise.hint = "Inside Leg (Inseam)"
                tilLength.hint = "Total Length"
            }
            "Jodhpuri" -> {
                tilShoulder.hint = "Shoulder Width"
                tilChest.hint = "Chest"
                tilCollar.hint = "Neck"
                tilSleeve.hint = "Sleeve Length"
                tilLength.hint = "Jacket Length"
                tilWaist.hint = "Stomach / Waist"
                tilHip.hint = "Hip"
                
                tilRise.visibility = View.GONE
            }
            "Shirt" -> {
                tilLength.hint = "Length"
                tilChest.hint = "Chest"
                tilWaist.hint = "Waist"
                tilCollar.hint = "Collar"
                tilShoulder.hint = "Shoulder"
                tilSleeve.hint = "Sleeve"
                
                tilHip.visibility = View.GONE
                tilRise.visibility = View.GONE
            }
        }
    }



    private fun saveAndSyncData() {
        val length = etLength.text.toString().trim()
        val chest = etChest.text.toString().trim()
        val waist = etWaist.text.toString().trim()
        val collar = etCollar.text.toString().trim()
        val shoulder = etShoulder.text.toString().trim()
        val sleeve = etSleeve.text.toString().trim()
        val hip = etHip.text.toString().trim()
        val rise = etRise.text.toString().trim()
        val notes = etNotes.text.toString().trim()
        val status = "Pending" 

        if (length.isEmpty() && chest.isEmpty() && waist.isEmpty() && collar.isEmpty() && 
            shoulder.isEmpty() && sleeve.isEmpty() && hip.isEmpty() && rise.isEmpty()) {
            Toast.makeText(this, "Please enter at least one measurement", Toast.LENGTH_SHORT).show()
            return
        }

        btnSaveMeasurements.isEnabled = false

        syncWithBackend(length, chest, waist, collar, shoulder, sleeve, hip, rise, notes, status)
    }

    private fun syncWithBackend(length: String, chest: String, waist: String, collar: String, shoulder: String, sleeve: String, hip: String, rise: String, notes: String, status: String) {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", 1).toString()
        
        val measurementData = mapOf(
            "user_id" to userId,
            "mobile_number" to customerMobile,
            "garment_type" to selectedGarment,
            "length" to length,
            "chest" to chest,
            "waist" to waist,
            "collar" to collar,
            "shoulder" to shoulder,
            "sleeve" to sleeve,
            "hip" to hip,
            "rise" to rise,
            "notes" to notes,
            "status" to status,
            "is_update" to isEditMode.toString()
        )

        RetrofitClient.instance.addMeasurement(measurementData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                btnSaveMeasurements.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@AddMeasurementsActivity, "Saved & Synced!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddMeasurementsActivity, "Sync failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnSaveMeasurements.isEnabled = true
                Toast.makeText(this@AddMeasurementsActivity, "Network Connection Failed", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun selectButton(selectedButton: MaterialButton, allButtons: List<MaterialButton>) {
        allButtons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundColor(Color.RED)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor(Color.WHITE)
                button.setTextColor(Color.parseColor("#333333"))
            }
        }
    }
}