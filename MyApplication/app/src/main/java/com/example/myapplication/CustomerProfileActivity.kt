package com.example.myapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    
    // UI References
    private var tvStatus: TextView? = null
    private var tvName: TextView? = null
    private var tvMobile: TextView? = null
    private var tvAddress: TextView? = null
    private var tvInitials: TextView? = null
    
    private var tvValue1: TextView? = null
    private var tvValue2: TextView? = null
    private var tvValue3: TextView? = null
    private var tvValue4: TextView? = null
    private var tvValue5: TextView? = null
    private var tvValue6: TextView? = null
    private var tvValue7: TextView? = null
    private var tvValue8: TextView? = null
    
    private var tvLabel1: TextView? = null
    private var tvLabel2: TextView? = null
    private var tvLabel3: TextView? = null
    private var tvLabel4: TextView? = null
    private var tvLabel5: TextView? = null
    private var tvLabel6: TextView? = null
    private var tvLabel7: TextView? = null
    private var tvLabel8: TextView? = null
    
    private var container1: View? = null
    private var container2: View? = null
    private var container3: View? = null
    private var container4: View? = null
    private var container5: View? = null
    private var container6: View? = null
    private var container7: View? = null
    private var container8: View? = null
    
    private var tvNotesValue: TextView? = null
    private var tvUnifiedTitle: TextView? = null
    private var btnEditUnified: MaterialButton? = null
    
    private var layoutMeasurementsCollapse: View? = null
    private var btnInProgress: MaterialButton? = null
    private var btnCompleted: MaterialButton? = null
    private var cardGarmentMain: MaterialCardView? = null
    private var cardMeasurementDisplay: MaterialCardView? = null
    private var layoutOrderActions: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.customer_profile)
            
            initViews()
            setupDataFromIntent()
            setupClickListeners()
            
            // Initial UI state
            updateGarmentButtonStyles(selectedGarment)
        } catch (e: Exception) {
            Log.e("CUST_PROFILE", "Critical error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error in Profile: ${e.message}", Toast.LENGTH_LONG).show()
            // finish() // Don't finish so I can see if anything rendered
        }
    }

    private fun initViews() {
        // Core Profile Views
        tvName = findViewById(R.id.tvName)
        tvMobile = findViewById(R.id.tvMobile)
        tvAddress = findViewById(R.id.tvAddress)
        tvStatus = findViewById(R.id.tvStatus)
        tvInitials = findViewById(R.id.tvInitials)
        
        // Dynamic Measurement Labels & Values
        tvLabel1 = findViewById(R.id.tvLabel1); tvValue1 = findViewById(R.id.tvValue1)
        tvLabel2 = findViewById(R.id.tvLabel2); tvValue2 = findViewById(R.id.tvValue2)
        tvLabel3 = findViewById(R.id.tvLabel3); tvValue3 = findViewById(R.id.tvValue3)
        tvLabel4 = findViewById(R.id.tvLabel4); tvValue4 = findViewById(R.id.tvValue4)
        tvLabel5 = findViewById(R.id.tvLabel5); tvValue5 = findViewById(R.id.tvValue5)
        tvLabel6 = findViewById(R.id.tvLabel6); tvValue6 = findViewById(R.id.tvValue6)
        tvLabel7 = findViewById(R.id.tvLabel7); tvValue7 = findViewById(R.id.tvValue7)
        tvLabel8 = findViewById(R.id.tvLabel8); tvValue8 = findViewById(R.id.tvValue8)
        
        // Measurement Row Containers
        container1 = findViewById(R.id.container1); container2 = findViewById(R.id.container2)
        container3 = findViewById(R.id.container3); container4 = findViewById(R.id.container4)
        container5 = findViewById(R.id.container5); container6 = findViewById(R.id.container6)
        container7 = findViewById(R.id.container7); container8 = findViewById(R.id.container8)

        tvNotesValue = findViewById(R.id.tvNotesValue)
        tvUnifiedTitle = findViewById(R.id.tvUnifiedGarmentTitle)
        btnEditUnified = findViewById(R.id.btnEditUnified)

        // Groups and Cards
        cardGarmentMain = findViewById(R.id.cardGarmentMain)
        cardMeasurementDisplay = findViewById(R.id.cardMeasurementDisplay)
        btnInProgress = findViewById(R.id.btnInProgress)
        btnCompleted = findViewById(R.id.btnCompleted)
        layoutMeasurementsCollapse = findViewById(R.id.layoutMeasurementsCollapse)
        layoutOrderActions = findViewById(R.id.layoutOrderActions)
    }

    private fun setupDataFromIntent() {
        currentCustomerName = intent.getStringExtra("CUSTOMER_NAME") ?: "Customer"
        currentMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
        selectedGarment = intent.getStringExtra("SELECTED_GARMENT") ?: "Shirt"

        tvName?.text = currentCustomerName
        tvMobile?.text = if (currentMobile.isNotEmpty()) currentMobile else "No Number"
        tvAddress?.text = "Loading address..."

        // Generate Initials safely
        val initials = try {
            if (currentCustomerName.isNotEmpty() && currentCustomerName != "Customer") {
                currentCustomerName.trim().split("\\s+".toRegex())
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .joinToString("")
                    .take(2)
                    .uppercase()
            } else "C"
        } catch (e: Exception) { "C" }
        tvInitials?.text = if (initials.isNullOrEmpty()) "C" else initials
    }

    private fun setupClickListeners() {
        // Back Button (Safe View cast)
        findViewById<View>(R.id.btnBackCard)?.setOnClickListener { 
            animateButtonClick(it)
            it.postDelayed({ finish() }, 150)
        }

        // Garment Selectors
        val garmentIds = mapOf(
            R.id.btnShirtProfile to "Shirt", R.id.btnPantProfile to "Pant",
            R.id.btnKotiProfile to "Koti", R.id.btnSuitProfile to "Suit",
            R.id.btnJabbhoProfile to "Jabbho", R.id.btnLehnghoProfile to "Lehngho",
            R.id.btnSafariProfile to "Safari", R.id.btnJodhpuriProfile to "Jodhpuri"
        )

        garmentIds.forEach { (id, type) ->
            findViewById<View>(id)?.setOnClickListener {
                animateButtonClick(it)
                if (selectedGarment != type) {
                    selectedGarment = type
                    updateGarmentButtonStyles(type)
                    showMeasurementDisplay(type)
                }
            }
        }

        // Action Buttons
        findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            animateButtonClick(it)
            val intent = Intent(this, AddCustomerActivity::class.java).apply {
                putExtra("CUSTOMER_NAME", currentCustomerName)
                putExtra("CUSTOMER_MOBILE", currentMobile)
                putExtra("IS_EDIT_MODE", true)
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.btnNewOrder)?.setOnClickListener {
            animateButtonClick(it)
            it.postDelayed({ showGarmentPickerBottomSheet() }, 150)
        }

        btnEditUnified?.setOnClickListener {
            animateButtonClick(it)
            it.postDelayed({ showMeasurementInputBottomSheet(true) }, 150)
        }

        btnInProgress?.setOnClickListener { 
            animateButtonClick(it)
            updateOrderStatus("In Progress") 
        }
        
        btnCompleted?.setOnClickListener { 
            animateButtonClick(it)
            updateOrderStatus("Completed") 
        }

        // Navigation
        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.setupGlobalNavigation(this, R.id.nav_customers)
    }

    private fun animateButtonClick(view: View) {
        try {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()
        } catch (e: Exception) { /* Ignore non-critical animation failures */ }
    }

    private fun updateGarmentButtonStyles(selectedType: String) {
        val garmentButtons = mapOf(
            "Shirt" to R.id.btnShirtProfile, "Pant" to R.id.btnPantProfile,
            "Koti" to R.id.btnKotiProfile, "Suit" to R.id.btnSuitProfile,
            "Jabbho" to R.id.btnJabbhoProfile, "Lehngho" to R.id.btnLehnghoProfile,
            "Safari" to R.id.btnSafariProfile, "Jodhpuri" to R.id.btnJodhpuriProfile
        )

        garmentButtons.forEach { (type, id) ->
            findViewById<MaterialButton>(id)?.let { btn ->
                if (type == selectedType) {
                    btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_dark))
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.divider))
                    btn.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))
                }
            }
        }
    }

    private fun showGarmentPickerBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_garment_picker, null)
        
        val buttonMap: Map<Int, String> = mapOf(
            R.id.cardPickShirt to "Shirt", R.id.cardPickPant to "Pant",
            R.id.cardPickKoti to "Koti", R.id.cardPickSuit to "Suit",
            R.id.cardPickJabbho to "Jabbho", R.id.cardPickLehngo to "Lehngho",
            R.id.cardPickSafari to "Safari", R.id.cardPickJodhpuri to "Jodhpuri"
        )

        buttonMap.forEach { (id, garment) ->
            view.findViewById<View>(id)?.setOnClickListener {
                animateButtonClick(it)
                selectedGarment = garment
                dialog.dismiss()
                showMeasurementInputBottomSheet(false)
            }
        }
        
        view.findViewById<View>(R.id.btnCloseSheet)?.setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btnSkipSelection)?.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (currentMobile.isNotEmpty()) {
            fetchCustomerData(currentMobile)
            loadLatestMeasurements(selectedGarment)
            fetchGarmentCounts()
        }
    }

    private fun showMeasurementDisplay(type: String) {
        cardMeasurementDisplay?.visibility = View.VISIBLE
        loadLatestMeasurements(type)
    }

    private fun fetchCustomerData(mobile: String) {
        RetrofitClient.instance.getCustomerDetails(mobile).enqueue(object : retrofit2.Callback<CustomerResponse> {
            override fun onResponse(call: retrofit2.Call<CustomerResponse>, response: retrofit2.Response<CustomerResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { customer ->
                        customerId = customer.id ?: -1
                        tvName?.text = customer.name ?: "Unknown"
                        tvMobile?.text = customer.mobileNumber ?: mobile
                        tvAddress?.text = if (customer.address.isNullOrEmpty()) "No address provided" else customer.address
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<CustomerResponse>, t: Throwable) {
                Log.e("CUST_PROFILE", "API Failure (Customer Details): ${t.message}")
            }
        })
    }

    private fun loadLatestMeasurements(type: String) {
        RetrofitClient.instance.getCustomerMeasurements(currentMobile, type).enqueue(object : retrofit2.Callback<MeasurementResponse> {
            override fun onResponse(call: retrofit2.Call<MeasurementResponse>, response: retrofit2.Response<MeasurementResponse>) {
                if (response.isSuccessful) {
                    val m = response.body()
                    if (m != null && m.id != null) {
                        tvUnifiedTitle?.text = "$type (${m.count ?: 0})"
                        btnEditUnified?.visibility = View.VISIBLE
                        tvNotesValue?.text = if (m.notes.isNullOrEmpty()) "None" else m.notes
                        
                        val status = m.status ?: "Pending"
                        tvStatus?.text = "Status: $status"
                        updateDisplayLabelsAndValues(type, m.length ?: "", m.chest ?: "", m.waist ?: "", m.collar ?: "", m.shoulder ?: "", m.sleeve ?: "", m.hip ?: "", m.rise ?: "")
                        
                        // Status styling logic
                        applyStatusStyle(status)
                    } else {
                        handleNoMeasurements(type)
                    }
                } else {
                    handleNoMeasurements(type)
                }
            }
            override fun onFailure(call: retrofit2.Call<MeasurementResponse>, t: Throwable) {
                handleNoMeasurements(type)
            }
        })
    }

    private fun applyStatusStyle(status: String) {
        val s = status.lowercase()
        when {
            s == "completed" || s == "ready" -> {
                tvStatus?.setTextColor(ContextCompat.getColor(this, R.color.success_text))
                tvStatus?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_bg))
                layoutOrderActions?.visibility = View.GONE
            }
            s == "in progress" -> {
                tvStatus?.setTextColor(Color.parseColor("#1E40AF")) // Deep Blue
                tvStatus?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DBEAFE"))
                layoutOrderActions?.visibility = View.VISIBLE
                btnInProgress?.isEnabled = false
                btnCompleted?.isEnabled = true
            }
            else -> { // Pending
                tvStatus?.setTextColor(ContextCompat.getColor(this, R.color.orange_primary))
                tvStatus?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF7ED"))
                layoutOrderActions?.visibility = View.VISIBLE
                btnInProgress?.isEnabled = true
                btnCompleted?.isEnabled = false
            }
        }
    }

    private fun handleNoMeasurements(type: String) {
        tvUnifiedTitle?.text = "$type (0)"
        btnEditUnified?.visibility = View.GONE
        tvNotesValue?.text = "None"
        tvStatus?.text = "No orders found"
        tvStatus?.setTextColor(Color.GRAY)
        tvStatus?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F3F4F6"))
        hideAllContainers()
        layoutOrderActions?.visibility = View.GONE
    }

    private fun updateOrderStatus(newStatus: String) {
        val data = mapOf("mobile_number" to currentMobile, "garment_type" to selectedGarment, "status" to newStatus)
        RetrofitClient.instance.updateOrderStatus(data).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CustomerProfileActivity, "Order marked as $newStatus", Toast.LENGTH_SHORT).show()
                    loadLatestMeasurements(selectedGarment)
                }
            }
            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Toast.makeText(this@CustomerProfileActivity, "Network error updating status", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDisplayLabelsAndValues(type: String, m1: String, m2: String, m3: String, m4: String, m5: String, m6: String, m7: String, m8: String) {
        hideAllContainers()
        when (type) {
            "Pant", "Lehngho" -> {
                setDisplayField(1, "Outseam", m1)
                setDisplayField(2, "Hip", m7)
                setDisplayField(3, "Waist", m3)
                setDisplayField(4, "Inseam", m4)
                setDisplayField(5, "Rise", m8)
            }
            "Shirt" -> {
                setDisplayField(1, "Length", m1)
                setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3)
                setDisplayField(4, "Collar", m4)
                setDisplayField(5, "Shoulder", m5)
                setDisplayField(6, "Sleeve", m6)
            }
            "Koti" -> {
                setDisplayField(1, "Length", m1)
                setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3)
                setDisplayField(5, "Shoulder", m5)
                setDisplayField(6, "Armhole", m6)
                setDisplayField(4, "Neck", m4)
            }
            "Suit", "Safari", "Jodhpuri" -> {
                setDisplayField(1, "Length", m1)
                setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Waist", m3)
                setDisplayField(4, "Shoulder", m5)
                setDisplayField(5, "Sleeve", m6)
                setDisplayField(6, "Collar", m4)
                if (type != "Suit") setDisplayField(7, "Hip", m7)
                if (type == "Safari") setDisplayField(8, "Inseam", m8)
            }
            "Jabbho" -> {
                setDisplayField(1, "Length", m1)
                setDisplayField(2, "Chest", m2)
                setDisplayField(3, "Shoulder", m5)
                setDisplayField(4, "Sleeve", m6)
                setDisplayField(5, "Neck", m4)
            }
        }
    }

    private fun showMeasurementInputBottomSheet(isEditMode: Boolean) {
        val dialog = BottomSheetDialog(this)
        val dialView = layoutInflater.inflate(R.layout.dialog_measurement_input, null)
        
        dialView.findViewById<TextView>(R.id.tvSheetTitle)?.text = "$selectedGarment Size"
        
        val ets = listOf(R.id.etSheet1, R.id.etSheet2, R.id.etSheet3, R.id.etSheet4, R.id.etSheet5, R.id.etSheet6, R.id.etSheet7, R.id.etSheet8).map { 
            dialView.findViewById<TextInputEditText>(it) 
        }
        val tils = listOf(R.id.tilSheet1, R.id.tilSheet2, R.id.tilSheet3, R.id.tilSheet4, R.id.tilSheet5, R.id.tilSheet6, R.id.tilSheet7, R.id.tilSheet8).map { 
            dialView.findViewById<TextInputLayout>(it) 
        }
        val etNotes = dialView.findViewById<TextInputEditText>(R.id.etSheetNotes)

        tils.forEach { it?.visibility = View.GONE }

        // Field Labeling Setup matches Database Keys: 0:length, 1:chest, 2:waist, 3:collar, 4:shoulder, 5:sleeve, 6:hip, 7:rise
        when (selectedGarment) {
            "Shirt" -> {
                setSheetField(tils[0], "Length"); setSheetField(tils[1], "Chest")
                setSheetField(tils[2], "Waist"); setSheetField(tils[3], "Collar")
                setSheetField(tils[4], "Shoulder"); setSheetField(tils[5], "Sleeve")
            }
            "Pant", "Lehngho" -> {
                setSheetField(tils[0], "Outseam"); setSheetField(tils[6], "Hip") // Use Hip field (index 6)
                setSheetField(tils[2], "Waist"); setSheetField(tils[3], "Inseam") // Use Collar field for Inseam (index 3)
                setSheetField(tils[7], "Rise") // Use Rise field (index 7)
            }
            "Koti" -> {
                setSheetField(tils[0], "Length"); setSheetField(tils[1], "Chest")
                setSheetField(tils[2], "Waist"); setSheetField(tils[4], "Shoulder")
                setSheetField(tils[5], "Armhole"); setSheetField(tils[3], "Neck")
            }
            "Suit", "Safari", "Jodhpuri" -> {
                setSheetField(tils[0], "Length"); setSheetField(tils[1], "Chest")
                setSheetField(tils[2], "Waist"); setSheetField(tils[4], "Shoulder")
                setSheetField(tils[5], "Sleeve"); setSheetField(tils[3], "Collar")
                if (selectedGarment != "Suit") setSheetField(tils[6], "Hip")
                if (selectedGarment == "Safari") setSheetField(tils[7], "Inseam") // Use Rise field for Inseam in Safari
            }
            "Jabbho" -> {
                setSheetField(tils[0], "Length"); setSheetField(tils[1], "Chest")
                setSheetField(tils[4], "Shoulder"); setSheetField(tils[5], "Sleeve")
                setSheetField(tils[3], "Neck")
            }
        }

        if (isEditMode) {
             RetrofitClient.instance.getCustomerMeasurements(currentMobile, selectedGarment).enqueue(object : retrofit2.Callback<MeasurementResponse> {
                override fun onResponse(call: retrofit2.Call<MeasurementResponse>, response: retrofit2.Response<MeasurementResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { m ->
                            ets[0]?.setText(m.length ?: "")
                            ets[1]?.setText(m.chest ?: "")
                            ets[2]?.setText(m.waist ?: "")
                            ets[3]?.setText(m.collar ?: "")
                            ets[4]?.setText(m.shoulder ?: "")
                            ets[5]?.setText(m.sleeve ?: "")
                            ets[6]?.setText(m.hip ?: "")
                            ets[7]?.setText(m.rise ?: "")
                            etNotes?.setText(m.notes ?: "")
                        }
                    }
                }
                override fun onFailure(call: retrofit2.Call<MeasurementResponse>, t: Throwable) {}
            })
        }

        dialView.findViewById<Button>(R.id.btnSheetSave)?.setOnClickListener {
            animateButtonClick(it)
            val data = mutableMapOf(
                "user_id" to getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("USER_ID", 1).toString(),
                "mobile_number" to currentMobile,
                "garment_type" to selectedGarment,
                "is_update" to isEditMode.toString(),
                "notes" to (etNotes?.text?.toString()?.trim() ?: "")
            )

            // Collect values correctly mapped to database keys
            data["length"] = ets[0]?.text?.toString()?.trim() ?: ""
            data["chest"] = ets[1]?.text?.toString()?.trim() ?: ""
            data["waist"] = ets[2]?.text?.toString()?.trim() ?: ""
            data["collar"] = ets[3]?.text?.toString()?.trim() ?: ""
            data["shoulder"] = ets[4]?.text?.toString()?.trim() ?: ""
            data["sleeve"] = ets[5]?.text?.toString()?.trim() ?: ""
            data["hip"] = ets[6]?.text?.toString()?.trim() ?: ""
            data["rise"] = ets[7]?.text?.toString()?.trim() ?: ""

            RetrofitClient.instance.addMeasurement(data).enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CustomerProfileActivity, "Measurement saved!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadLatestMeasurements(selectedGarment)
                        fetchGarmentCounts()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    Toast.makeText(this@CustomerProfileActivity, "Failed to sync with server", Toast.LENGTH_SHORT).show()
                }
            })
        }

        dialog.setContentView(dialView)
        dialog.show()
    }

    private fun setSheetField(til: TextInputLayout?, hint: String) {
        til?.let {
            it.hint = hint
            it.visibility = View.VISIBLE
        }
    }

    private fun setDisplayField(index: Int, label: String, value: String) {
        val container = when(index) { 1->container1; 2->container2; 3->container3; 4->container4; 5->container5; 6->container6; 7->container7; 8->container8; else->null }
        val labelTv = when(index) { 1->tvLabel1; 2->tvLabel2; 3->tvLabel3; 4->tvLabel4; 5->tvLabel5; 6->tvLabel6; 7->tvLabel7; 8->tvLabel8; else->null }
        val valueTv = when(index) { 1->tvValue1; 2->tvValue2; 3->tvValue3; 4->tvValue4; 5->tvValue5; 6->tvValue6; 7->tvValue7; 8->tvValue8; else->null }
        
        container?.visibility = View.VISIBLE
        labelTv?.text = label
        valueTv?.text = if (value.isEmpty()) "-" else "$value in"
    }

    private fun hideAllContainers() {
        listOf(container1, container2, container3, container4, container5, container6, container7, container8).forEach { it?.visibility = View.GONE }
    }

    private fun fetchGarmentCounts() {
        val types = listOf("Shirt", "Pant", "Koti", "Suit", "Jabbho", "Lehngho", "Safari", "Jodhpuri")
        types.forEach { type ->
            RetrofitClient.instance.getCustomerMeasurements(currentMobile, type).enqueue(object : retrofit2.Callback<MeasurementResponse> {
                override fun onResponse(call: retrofit2.Call<MeasurementResponse>, response: retrofit2.Response<MeasurementResponse>) {
                    if (response.isSuccessful) {
                        val count = response.body()?.count ?: 0
                        val buttonId = when(type) {
                            "Shirt" -> R.id.btnShirtProfile; "Pant" -> R.id.btnPantProfile
                            "Koti" -> R.id.btnKotiProfile; "Suit" -> R.id.btnSuitProfile
                            "Jabbho" -> R.id.btnJabbhoProfile; "Lehngho" -> R.id.btnLehnghoProfile
                            "Safari" -> R.id.btnSafariProfile; "Jodhpuri" -> R.id.btnJodhpuriProfile
                            else -> null
                        }
                        buttonId?.let { id -> findViewById<MaterialButton>(id)?.text = "$type ($count)" }
                    }
                }
                override fun onFailure(callPath: retrofit2.Call<MeasurementResponse>, t: Throwable) {}
            })
        }
    }
}
