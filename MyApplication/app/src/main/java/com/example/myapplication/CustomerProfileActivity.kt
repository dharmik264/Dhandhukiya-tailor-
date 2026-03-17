package com.example.myapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView

class CustomerProfileActivity : AppCompatActivity() {

    private var customerId: Int = -1
    private var currentMobile: String = ""
    private var currentCustomerName: String = ""
    private var selectedGarment: String = "Shirt"
    
    // UI References - Display
    private lateinit var tvStatus: TextView
    private lateinit var tvName: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvInitials: TextView
    
    private lateinit var tvValue1: TextView
    private lateinit var tvValue2: TextView
    private lateinit var tvValue3: TextView
    private lateinit var tvValue4: TextView
    private lateinit var tvValue5: TextView
    private lateinit var tvValue6: TextView
    private lateinit var tvValue7: TextView
    private lateinit var tvValue8: TextView
    
    private lateinit var tvLabel1: TextView
    private lateinit var tvLabel2: TextView
    private lateinit var tvLabel3: TextView
    private lateinit var tvLabel4: TextView
    private lateinit var tvLabel5: TextView
    private lateinit var tvLabel6: TextView
    private lateinit var tvLabel7: TextView
    private lateinit var tvLabel8: TextView
    
    private lateinit var container1: View
    private lateinit var container2: View
    private lateinit var container3: View
    private lateinit var container4: View
    private lateinit var container5: View
    private lateinit var container6: View
    private lateinit var container7: View
    private lateinit var container8: View
    
    private lateinit var tvNotesValue: TextView
    private lateinit var tvUnifiedTitle: TextView
    private lateinit var btnEditUnified: MaterialButton
    
    private lateinit var layoutMeasurementsCollapse: View
    private lateinit var btnInProgress: MaterialButton
    private lateinit var btnCompleted: MaterialButton
    private lateinit var displayGrid: GridLayout
    private lateinit var cardGarmentMain: MaterialCardView
    private lateinit var cardMeasurementDisplay: MaterialCardView
    private lateinit var layoutOrderActions: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_profile)


        // Initialize Display Views
        tvName = findViewById(R.id.tvName)
        tvMobile = findViewById(R.id.tvMobile)
        tvAddress = findViewById(R.id.tvAddress)
        tvStatus = findViewById(R.id.tvStatus)
        tvInitials = findViewById(R.id.tvInitials)
        
        tvValue1 = findViewById(R.id.tvValue1)
        tvValue2 = findViewById(R.id.tvValue2)
        tvValue3 = findViewById(R.id.tvValue3)
        tvValue4 = findViewById(R.id.tvValue4)
        tvValue5 = findViewById(R.id.tvValue5)
        tvValue6 = findViewById(R.id.tvValue6)
        tvValue7 = findViewById(R.id.tvValue7)
        tvValue8 = findViewById(R.id.tvValue8)
        
        tvLabel1 = findViewById(R.id.tvLabel1)
        tvLabel2 = findViewById(R.id.tvLabel2)
        tvLabel3 = findViewById(R.id.tvLabel3)
        tvLabel4 = findViewById(R.id.tvLabel4)
        tvLabel5 = findViewById(R.id.tvLabel5)
        tvLabel6 = findViewById(R.id.tvLabel6)
        tvLabel7 = findViewById(R.id.tvLabel7)
        tvLabel8 = findViewById(R.id.tvLabel8)
        
        container1 = findViewById(R.id.container1)
        container2 = findViewById(R.id.container2)
        container3 = findViewById(R.id.container3)
        container4 = findViewById(R.id.container4)
        container5 = findViewById(R.id.container5)
        container6 = findViewById(R.id.container6)
        container7 = findViewById(R.id.container7)
        container8 = findViewById(R.id.container8)

        tvNotesValue = findViewById(R.id.tvNotesValue)
        tvUnifiedTitle = findViewById(R.id.tvUnifiedGarmentTitle)
        btnEditUnified = findViewById(R.id.btnEditUnified)

        cardGarmentMain = findViewById(R.id.cardGarmentMain)
        cardMeasurementDisplay = findViewById(R.id.cardMeasurementDisplay)
        btnInProgress = findViewById(R.id.btnInProgress)
        btnCompleted = findViewById(R.id.btnCompleted)
        displayGrid = findViewById(R.id.displayGrid)
        layoutMeasurementsCollapse = findViewById(R.id.layoutMeasurementsCollapse)
        layoutOrderActions = findViewById(R.id.layoutOrderActions)


        // Data from intent
        currentCustomerName = intent.getStringExtra("CUSTOMER_NAME") ?: "Unknown"
        currentMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
        selectedGarment = intent.getStringExtra("SELECTED_GARMENT") ?: "Shirt"

        tvName.text = currentCustomerName
        tvMobile.text = if (currentMobile.isNotEmpty()) currentMobile else "—"

        val initials = currentCustomerName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
        tvInitials.text = if (initials.isNotEmpty()) initials else "C"

        if (currentMobile.isNotEmpty()) {
            fetchCustomerData(currentMobile)
        } else {
            // Mobile missing — show empty state
            tvAddress.text = "No Address"
            checkAndShowUI(selectedGarment)
        }

        findViewById<View>(R.id.btnBackCard).setOnClickListener { 
            animateButtonClick(it)
            it.postDelayed({ finish() }, 150)
        }

        // Setup All Garment Buttons
        val garmentButtons = mapOf(
            R.id.btnShirtProfile to "Shirt",
            R.id.btnPantProfile to "Pant",
            R.id.btnKotiProfile to "Koti",
            R.id.btnSuitProfile to "Suit",
            R.id.btnJabbhoProfile to "Jabbho",
            R.id.btnLehnghoProfile to "Lehngho",
            R.id.btnSafariProfile to "Safari",
            R.id.btnJodhpuriProfile to "Jodhpuri"
        )

        garmentButtons.forEach { (id, type) ->
            findViewById<MaterialButton>(id).setOnClickListener {
                animateButtonClick(it)
                selectedGarment = type
                updateGarmentButtonStyles(type)
                checkAndShowUI(type)
            }
        }

        findViewById<Button>(R.id.btnNewOrder).setOnClickListener {
            animateButtonClick(it)
            it.postDelayed({ showGarmentPickerBottomSheet() }, 150)
        }


        btnEditUnified.setOnClickListener {
            animateButtonClick(it)
            it.postDelayed({
                selectedGarment = tvUnifiedTitle.text.toString().split(" ")[0]
                showMeasurementInputBottomSheet(true)
            }, 150)
        }

        btnInProgress.setOnClickListener { 
            animateButtonClick(it)
            updateOrderStatus("In Progress") 
        }
        btnCompleted.setOnClickListener { 
            animateButtonClick(it)
            updateOrderStatus("Completed") 
        }

        updateGarmentButtonStyles(selectedGarment)
        fetchGarmentCounts()

        // Setup Bottom Navigation
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

    private fun updateGarmentButtonStyles(selectedType: String) {
        val garmentButtons = mapOf(
            "Shirt" to R.id.btnShirtProfile,
            "Pant" to R.id.btnPantProfile,
            "Koti" to R.id.btnKotiProfile,
            "Suit" to R.id.btnSuitProfile,
            "Jabbho" to R.id.btnJabbhoProfile,
            "Lehngho" to R.id.btnLehnghoProfile,
            "Safari" to R.id.btnSafariProfile,
            "Jodhpuri" to R.id.btnJodhpuriProfile
        )

        garmentButtons.forEach { (type, id) ->
            val btn = findViewById<MaterialButton>(id)
            if (type == selectedType) {
                btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.red_dark, null))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.divider, null))
                btn.setTextColor(resources.getColor(R.color.on_surface_variant, null))
            }
        }
    }

    private fun showGarmentPickerBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_garment_picker, null)
        
        val buttonMap = mapOf(
            R.id.btnPickShirt to "Shirt",
            R.id.btnPickPant to "Pant",
            R.id.btnPickKoti to "Koti",
            R.id.btnPickSuit to "Suit",
            R.id.btnPickJabbho to "Jabbho",
            R.id.btnPickLehngho to "Lehngho",
            R.id.btnPickSafari to "Safari",
            R.id.btnPickJodhpuri to "Jodhpuri"
        )

        buttonMap.forEach { (id, garment) ->
            view.findViewById<Button>(id)?.setOnClickListener {
                animateButtonClick(it)
                selectedGarment = garment
                dialog.dismiss()
                showMeasurementInputBottomSheet(false)
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (currentMobile.isNotEmpty()) {
            checkAndShowUI(selectedGarment)
        }
    }

    private fun checkAndShowUI(type: String) {
        RetrofitClient.instance.getCustomerMeasurements(currentMobile, type).enqueue(object : retrofit2.Callback<MeasurementResponse> {
            override fun onResponse(call: retrofit2.Call<MeasurementResponse>, response: retrofit2.Response<MeasurementResponse>) {
                showMeasurementDisplay(type)
            }

            override fun onFailure(call: retrofit2.Call<MeasurementResponse>, t: Throwable) {
                showMeasurementDisplay(type)
            }
        })
    }

    private fun showMeasurementDisplay(type: String) {
        cardMeasurementDisplay.visibility = View.VISIBLE
        loadLatestMeasurements(type)
    }


    private fun fetchCustomerData(mobile: String) {
        RetrofitClient.instance.getCustomerDetails(mobile).enqueue(object : retrofit2.Callback<CustomerResponse> {
            override fun onResponse(call: retrofit2.Call<CustomerResponse>, response: retrofit2.Response<CustomerResponse>) {
                if (response.isSuccessful) {
                    val customer = response.body()
                    if (customer != null) {
                        customerId = customer.id ?: -1
                        val name = customer.name ?: "Unknown"
                        val mobileNum = customer.mobileNumber ?: "No Number"
                        tvName.text = name
                        tvMobile.text = mobileNum
                        tvAddress.text = if (customer.address.isNullOrEmpty()) "No Address Provided" else customer.address
                        
                        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
                        tvInitials.text = if (initials.isNotEmpty()) initials else "C"
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<CustomerResponse>, t: Throwable) {
                Log.e("CUSTOMER_PROFILE", "Error fetching customer details: ${t.message}")
            }
        })
        checkAndShowUI(selectedGarment)
    }

    private fun loadLatestMeasurements(type: String) {
        RetrofitClient.instance.getCustomerMeasurements(currentMobile, type).enqueue(object : retrofit2.Callback<MeasurementResponse> {
            override fun onResponse(call: retrofit2.Call<MeasurementResponse>, response: retrofit2.Response<MeasurementResponse>) {
                if (response.isSuccessful) {
                    val measurement = response.body()
                    if (measurement != null && measurement.id != null) {
                        val count = measurement.count ?: 0
                        tvUnifiedTitle.text = "$type ($count)"
                        btnEditUnified.visibility = View.VISIBLE
                        tvStatus.text = "Status: ${measurement.status ?: "Pending"}"
                        tvNotesValue.text = if (measurement.notes.isNullOrEmpty()) "None" else measurement.notes
                        updateDisplayLabelsAndValues(type, 
                            measurement.length ?: "", 
                            measurement.chest ?: "", 
                            measurement.waist ?: "", 
                            measurement.collar ?: "", 
                            measurement.shoulder ?: "", 
                            measurement.sleeve ?: "", 
                            measurement.hip ?: "", 
                            measurement.rise ?: "")
                        
                        // Update buttons based on status
                        val currentStatus = measurement.status ?: "Pending"
                        when (currentStatus.lowercase()) {
                            "pending" -> {
                                btnInProgress.isEnabled = true
                                btnCompleted.isEnabled = false
                                layoutOrderActions.visibility = View.VISIBLE
                            }
                            "in progress" -> {
                                btnInProgress.isEnabled = false
                                btnCompleted.isEnabled = true
                                layoutOrderActions.visibility = View.VISIBLE
                            }
                            "completed" -> {
                                btnInProgress.isEnabled = false
                                btnCompleted.isEnabled = false
                                layoutOrderActions.visibility = View.GONE
                            }
                            else -> {
                                layoutOrderActions.visibility = View.GONE
                            }
                        }
                    } else {
                        handleNoMeasurements(type)
                    }
                } else {
                    handleNoMeasurements(type)
                }
            }

            override fun onFailure(call: retrofit2.Call<MeasurementResponse>, t: Throwable) {
                tvUnifiedTitle.text = "$type (Network Issue)"
                hideAllContainers()
                layoutOrderActions.visibility = View.GONE
            }
        })
    }

    private fun handleNoMeasurements(type: String) {
        tvUnifiedTitle.text = "$type (0)"
        btnEditUnified.visibility = View.GONE
        hideAllContainers()
        tvNotesValue.text = "None"
        tvStatus.text = "Status: None"
        layoutOrderActions.visibility = View.GONE
    }

    private fun updateOrderStatus(newStatus: String) {
        val data = mapOf(
            "mobile_number" to currentMobile,
            "garment_type" to selectedGarment,
            "status" to newStatus
        )

        RetrofitClient.instance.updateOrderStatus(data).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CustomerProfileActivity, "Order is now $newStatus", Toast.LENGTH_SHORT).show()
                    loadLatestMeasurements(selectedGarment)
                } else {
                    Toast.makeText(this@CustomerProfileActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Toast.makeText(this@CustomerProfileActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDisplayLabelsAndValues(type: String, m1: String, m2: String, m3: String, m4: String, m5: String, m6: String, m7: String, m8: String) {
        hideAllContainers()
        when (type) {
            "Pant" -> {
                setDisplayField(1, "Waist", m3); setDisplayField(2, "Hip", m7)
                setDisplayField(3, "Inseam", m4); setDisplayField(4, "Outseam", m1)
                setDisplayField(5, "Rise", m8)
            }
            "Shirt" -> {
                setDisplayField(1, "Length", m1); setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3); setDisplayField(4, "Collar", m4)
                setDisplayField(5, "Shoulder", m5); setDisplayField(6, "Sleeve", m6)
            }
            "Koti" -> {
                setDisplayField(1, "Length", m1); setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3); setDisplayField(4, "Shoulder", m5)
                setDisplayField(5, "Armhole", m6); setDisplayField(6, "Neckline", m4)
            }
            "Suit" -> {
                setDisplayField(1, "Length", m1); setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3); setDisplayField(4, "Shoulder", m5)
                setDisplayField(5, "Sleeve", m6); setDisplayField(6, "Inseam", m4)
            }
            "Jabbho" -> {
                setDisplayField(1, "Length", m1); setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Shoulder", m5); setDisplayField(4, "Sleeve", m6)
                setDisplayField(5, "Neck", m4)
            }
            "Lehngho" -> {
                setDisplayField(1, "Waist", m3); setDisplayField(2, "Hip", m7)
                setDisplayField(3, "Inseam", m4); setDisplayField(4, "Outseam", m1)
                setDisplayField(5, "Rise", m8)
            }
            "Safari" -> {
                setDisplayField(1, "Length", m1); setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3); setDisplayField(4, "Shoulder", m5)
                setDisplayField(5, "Sleeve", m6); setDisplayField(6, "Neck", m4)
                setDisplayField(7, "Hip", m7); setDisplayField(8, "Inseam", m8)
            }
            "Jodhpuri" -> {
                setDisplayField(1, "Length", m1); setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3); setDisplayField(4, "Shoulder", m5)
                setDisplayField(5, "Sleeve", m6); setDisplayField(6, "Neck", m4)
                setDisplayField(7, "Hip", m7)
            }
        }
    }

    private fun showMeasurementInputBottomSheet(isEditMode: Boolean) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_measurement_input, null)
        
        val tvSheetTitle = view.findViewById<TextView>(R.id.tvSheetTitle)
        tvSheetTitle.text = "$selectedGarment Size"
        
        val etSheet1 = view.findViewById<TextInputEditText>(R.id.etSheet1)
        val etSheet2 = view.findViewById<TextInputEditText>(R.id.etSheet2)
        val etSheet3 = view.findViewById<TextInputEditText>(R.id.etSheet3)
        val etSheet4 = view.findViewById<TextInputEditText>(R.id.etSheet4)
        val etSheet5 = view.findViewById<TextInputEditText>(R.id.etSheet5)
        val etSheet6 = view.findViewById<TextInputEditText>(R.id.etSheet6)
        val etSheet7 = view.findViewById<TextInputEditText>(R.id.etSheet7)
        val etSheet8 = view.findViewById<TextInputEditText>(R.id.etSheet8)
        val etSheetNotes = view.findViewById<TextInputEditText>(R.id.etSheetNotes)
        
        val tilSheet1 = view.findViewById<TextInputLayout>(R.id.tilSheet1)
        val tilSheet2 = view.findViewById<TextInputLayout>(R.id.tilSheet2)
        val tilSheet3 = view.findViewById<TextInputLayout>(R.id.tilSheet3)
        val tilSheet4 = view.findViewById<TextInputLayout>(R.id.tilSheet4)
        val tilSheet5 = view.findViewById<TextInputLayout>(R.id.tilSheet5)
        val tilSheet6 = view.findViewById<TextInputLayout>(R.id.tilSheet6)
        val tilSheet7 = view.findViewById<TextInputLayout>(R.id.tilSheet7)
        val tilSheet8 = view.findViewById<TextInputLayout>(R.id.tilSheet8)

        // Setup Hints and Visibility based on garment
        val allTils = listOf(tilSheet1, tilSheet2, tilSheet3, tilSheet4, tilSheet5, tilSheet6, tilSheet7, tilSheet8)
        allTils.forEach { it?.visibility = View.GONE }

        when (selectedGarment) {
            "Shirt" -> {
                setSheetField(tilSheet1, "Length", true); setSheetField(tilSheet2, "Chest", true)
                setSheetField(tilSheet3, "Waist", true); setSheetField(tilSheet4, "Collar", true)
                setSheetField(tilSheet5, "Shoulder", true); setSheetField(tilSheet6, "Sleeve", true)
            }
            "Pant" -> {
                setSheetField(tilSheet1, "Outseam", true); setSheetField(tilSheet2, "Hip", true)
                setSheetField(tilSheet3, "Waist", true); setSheetField(tilSheet4, "Inseam", true)
                setSheetField(tilSheet5, "Rise", true)
            }
            "Koti", "Suit", "Safari", "Jodhpuri" -> {
                setSheetField(tilSheet1, "Length", true); setSheetField(tilSheet2, "Chest", true)
                setSheetField(tilSheet3, "Waist", true); setSheetField(tilSheet4, "Shoulder", true)
                setSheetField(tilSheet5, "Sleeve", true); setSheetField(tilSheet6, "Collar", true)
                if (selectedGarment == "Safari" || selectedGarment == "Jodhpuri") {
                    setSheetField(tilSheet7, "Hip", true)
                    if (selectedGarment == "Safari") setSheetField(tilSheet8, "Inseam", true)
                }
            }
            "Jabbho" -> {
                setSheetField(tilSheet1, "Length", true); setSheetField(tilSheet2, "Chest", true)
                setSheetField(tilSheet3, "Shoulder", true); setSheetField(tilSheet4, "Sleeve", true)
                setSheetField(tilSheet5, "Collar", true)
            }
            "Lehngho" -> {
                setSheetField(tilSheet1, "Outseam", true); setSheetField(tilSheet2, "Hip", true)
                setSheetField(tilSheet3, "Waist", true); setSheetField(tilSheet4, "Inseam", true)
                setSheetField(tilSheet5, "Rise", true)
            }
        }

        // If Edit Mode, fill values
        if (isEditMode) {
             RetrofitClient.instance.getCustomerMeasurements(currentMobile, selectedGarment).enqueue(object : retrofit2.Callback<MeasurementResponse> {
                override fun onResponse(call: retrofit2.Call<MeasurementResponse>, response: retrofit2.Response<MeasurementResponse>) {
                    if (response.isSuccessful) {
                        val m = response.body()
                        if (m != null) {
                            etSheet1?.setText(m.length ?: "")
                            etSheet2?.setText(m.chest ?: "")
                            etSheet3?.setText(m.waist ?: "")
                            etSheet4?.setText(m.collar ?: "")
                            etSheet5?.setText(m.shoulder ?: "")
                            etSheet6?.setText(m.sleeve ?: "")
                            etSheet7?.setText(m.hip ?: "")
                            etSheet8?.setText(m.rise ?: "")
                            etSheetNotes?.setText(m.notes ?: "")
                        }
                    }
                }
                override fun onFailure(call: retrofit2.Call<MeasurementResponse>, t: Throwable) {}
            })
        }

        view.findViewById<Button>(R.id.btnSheetSave).setOnClickListener {
            animateButtonClick(it)
            val v1 = etSheet1?.text.toString().trim()
            val v2 = etSheet2?.text.toString().trim()
            val v3 = etSheet3?.text.toString().trim()
            val v4 = etSheet4?.text.toString().trim()
            val v5 = etSheet5?.text.toString().trim()
            val v6 = etSheet6?.text.toString().trim()
            val v7 = etSheet7?.text.toString().trim()
            val v8 = etSheet8?.text.toString().trim()
            val notes = etSheetNotes?.text.toString().trim()

            val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
            val userIdString = sharedPref.getInt("USER_ID", 1).toString()

            // Dynamic Mapping based on Garment Type to match Sheet Hints
            val data = mutableMapOf(
                "user_id" to userIdString,
                "mobile_number" to currentMobile,
                "garment_type" to selectedGarment,
                "is_update" to isEditMode.toString(),
                "notes" to notes
            )

            when (selectedGarment) {
                "Shirt" -> {
                    data["length"] = v1; data["chest"] = v2; data["waist"] = v3
                    data["collar"] = v4; data["shoulder"] = v5; data["sleeve"] = v6
                }
                "Pant", "Lehngho" -> {
                    data["length"] = v1; data["hip"] = v2; data["waist"] = v3
                    data["collar"] = v4; data["rise"] = v5
                }
                "Koti", "Suit", "Safari", "Jodhpuri" -> {
                    data["length"] = v1; data["chest"] = v2; data["waist"] = v3
                    data["shoulder"] = v4; data["sleeve"] = v5; data["collar"] = v6
                    data["hip"] = v7; data["rise"] = v8
                }
                "Jabbho" -> {
                    data["length"] = v1; data["chest"] = v2; data["shoulder"] = v3
                    data["sleeve"] = v4; data["collar"] = v5
                }
            }

            RetrofitClient.instance.addMeasurement(data).enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CustomerProfileActivity, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        checkAndShowUI(selectedGarment)
                        fetchGarmentCounts()
                    } else {
                        Toast.makeText(this@CustomerProfileActivity, "Error saving", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    Toast.makeText(this@CustomerProfileActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun setSheetField(til: TextInputLayout?, hint: String, visible: Boolean) {
        til?.let {
            it.hint = hint
            it.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun fetchGarmentCounts() {
        val garmentTypes = listOf("Shirt", "Pant", "Koti", "Suit", "Jabbho", "Lehngho", "Safari", "Jodhpuri")
        garmentTypes.forEach { type ->
            RetrofitClient.instance.getCustomerMeasurements(currentMobile, type).enqueue(object : retrofit2.Callback<MeasurementResponse> {
                override fun onResponse(call: retrofit2.Call<MeasurementResponse>, response: retrofit2.Response<MeasurementResponse>) {
                    if (response.isSuccessful) {
                        val count = response.body()?.count ?: 0
                        updateButtonCount(type, count)
                    } else {
                        updateButtonCount(type, 0)
                    }
                }
                override fun onFailure(call: retrofit2.Call<MeasurementResponse>, t: Throwable) {
                    updateButtonCount(type, 0)
                }
            })
        }
    }




    private fun updateButtonCount(type: String, count: Int) {
        val buttonId = when(type) {
            "Shirt" -> R.id.btnShirtProfile
            "Pant" -> R.id.btnPantProfile
            "Koti" -> R.id.btnKotiProfile
            "Suit" -> R.id.btnSuitProfile
            "Jabbho" -> R.id.btnJabbhoProfile
            "Lehngho" -> R.id.btnLehnghoProfile
            "Safari" -> R.id.btnSafariProfile
            "Jodhpuri" -> R.id.btnJodhpuriProfile
            else -> null
        }
        buttonId?.let { id ->
            findViewById<MaterialButton>(id)?.let { btn ->
                btn.text = "$type ($count)"
            }
        }
    }

    private fun setDisplayField(index: Int, label: String, value: String) {
        val container = when(index) {
            1 -> container1; 2 -> container2; 3 -> container3; 4 -> container4
            5 -> container5; 6 -> container6; 7 -> container7; 8 -> container8
            else -> container1
        }
        val labelTv = when(index) {
            1 -> tvLabel1; 2 -> tvLabel2; 3 -> tvLabel3; 4 -> tvLabel4
            5 -> tvLabel5; 6 -> tvLabel6; 7 -> tvLabel7; 8 -> tvLabel8
            else -> tvLabel1
        }
        val targetValueTv = when(index) {
            1 -> tvValue1; 2 -> tvValue2; 3 -> tvValue3; 4 -> tvValue4
            5 -> tvValue5; 6 -> tvValue6; 7 -> tvValue7; 8 -> tvValue8
            else -> tvValue1
        }
        
        container.visibility = View.VISIBLE
        labelTv.text = label
        targetValueTv.text = if (value.isEmpty()) "-" else "$value in"
    }

    private fun hideAllContainers() {
        val containers = listOf(container1, container2, container3, container4, container5, container6, container7, container8)
        containers.forEach { it.visibility = View.GONE }
    }
}