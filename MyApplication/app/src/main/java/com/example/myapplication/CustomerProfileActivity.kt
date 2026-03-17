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
        setContentView(R.layout.customer_profile)

        initViews()

        currentCustomerName = intent.getStringExtra("CUSTOMER_NAME") ?: "Unknown"
        currentMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
        selectedGarment = intent.getStringExtra("SELECTED_GARMENT") ?: "Shirt"

        tvName?.text = currentCustomerName
        tvMobile?.text = currentMobile.ifEmpty { "—" }
        tvAddress?.text = "Loading details..."

        val initials = if (currentCustomerName.isNotEmpty() && currentCustomerName != "Unknown") {
            currentCustomerName.trim().split("\\s+".toRegex())
                .mapNotNull { it.firstOrNull()?.toString() }
                .joinToString("")
                .take(2)
                .uppercase()
        } else "C"
        tvInitials?.text = if (initials.isNotEmpty()) initials else "C"

        setupListeners()
        updateGarmentButtonStyles(selectedGarment)
    }

    private fun initViews() {
        tvName = findViewById<TextView>(R.id.tvName)
        tvMobile = findViewById<TextView>(R.id.tvMobile)
        tvAddress = findViewById<TextView>(R.id.tvAddress)
        tvStatus = findViewById<TextView>(R.id.tvStatus)
        tvInitials = findViewById<TextView>(R.id.tvInitials)
        
        tvValue1 = findViewById<TextView>(R.id.tvValue1)
        tvValue2 = findViewById<TextView>(R.id.tvValue2)
        tvValue3 = findViewById<TextView>(R.id.tvValue3)
        tvValue4 = findViewById<TextView>(R.id.tvValue4)
        tvValue5 = findViewById<TextView>(R.id.tvValue5)
        tvValue6 = findViewById<TextView>(R.id.tvValue6)
        tvValue7 = findViewById<TextView>(R.id.tvValue7)
        tvValue8 = findViewById<TextView>(R.id.tvValue8)
        
        tvLabel1 = findViewById<TextView>(R.id.tvLabel1)
        tvLabel2 = findViewById<TextView>(R.id.tvLabel2)
        tvLabel3 = findViewById<TextView>(R.id.tvLabel3)
        tvLabel4 = findViewById<TextView>(R.id.tvLabel4)
        tvLabel5 = findViewById<TextView>(R.id.tvLabel5)
        tvLabel6 = findViewById<TextView>(R.id.tvLabel6)
        tvLabel7 = findViewById<TextView>(R.id.tvLabel7)
        tvLabel8 = findViewById<TextView>(R.id.tvLabel8)
        
        container1 = findViewById<View>(R.id.container1)
        container2 = findViewById<View>(R.id.container2)
        container3 = findViewById<View>(R.id.container3)
        container4 = findViewById<View>(R.id.container4)
        container5 = findViewById<View>(R.id.container5)
        container6 = findViewById<View>(R.id.container6)
        container7 = findViewById<View>(R.id.container7)
        container8 = findViewById<View>(R.id.container8)

        tvNotesValue = findViewById<TextView>(R.id.tvNotesValue)
        tvUnifiedTitle = findViewById<TextView>(R.id.tvUnifiedGarmentTitle)
        btnEditUnified = findViewById<MaterialButton>(R.id.btnEditUnified)

        cardGarmentMain = findViewById<MaterialCardView>(R.id.cardGarmentMain)
        cardMeasurementDisplay = findViewById<MaterialCardView>(R.id.cardMeasurementDisplay)
        btnInProgress = findViewById<MaterialButton>(R.id.btnInProgress)
        btnCompleted = findViewById<MaterialButton>(R.id.btnCompleted)
        layoutMeasurementsCollapse = findViewById<View>(R.id.layoutMeasurementsCollapse)
        layoutOrderActions = findViewById<View>(R.id.layoutOrderActions)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBackCard)?.setOnClickListener { 
            animateButtonClick(it)
            it.postDelayed({ finish() }, 150)
        }

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
            findViewById<MaterialButton>(id)?.setOnClickListener {
                animateButtonClick(it)
                if (selectedGarment != type) {
                    selectedGarment = type
                    updateGarmentButtonStyles(type)
                    checkAndShowUI(type)
                }
            }
        }

        findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            animateButtonClick(it)
            val intent = Intent(this, AddCustomerActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", currentCustomerName)
            intent.putExtra("CUSTOMER_MOBILE", currentMobile)
            intent.putExtra("IS_EDIT_MODE", true)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnNewOrder)?.setOnClickListener {
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

        findViewById<BottomNavigationView>(R.id.mainBottomNavigation)?.let {
            it.setupGlobalNavigation(this, R.id.nav_customers)
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
            R.id.cardPickShirt to "Shirt",
            R.id.cardPickPant to "Pant",
            R.id.cardPickKoti to "Koti",
            R.id.cardPickSuit to "Suit",
            R.id.cardPickJabbho to "Jabbho",
            R.id.cardPickLehngo to "Lehngho",
            R.id.cardPickSafari to "Safari",
            R.id.cardPickJodhpuri to "Jodhpuri"
        )

        buttonMap.forEach { (id, garment) ->
            view.findViewById<View>(id)?.setOnClickListener {
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
        if (currentMobile.isNotEmpty() && currentMobile != "No Number") {
            fetchCustomerData(currentMobile)
            loadLatestMeasurements(selectedGarment)
        }
    }

    private fun checkAndShowUI(type: String) {
        showMeasurementDisplay(type)
    }

    private fun showMeasurementDisplay(type: String) {
        cardMeasurementDisplay?.visibility = View.VISIBLE
        loadLatestMeasurements(type)
    }

    private fun fetchCustomerData(mobile: String) {
        RetrofitClient.instance.getCustomerDetails(mobile).enqueue(object : retrofit2.Callback<CustomerResponse> {
            override fun onResponse(call: retrofit2.Call<CustomerResponse>, response: retrofit2.Response<CustomerResponse>) {
                if (response.isSuccessful) {
                    val customer = response.body()
                    if (customer != null) {
                        customerId = customer.id ?: -1
                        tvName?.text = customer.name ?: "Unknown"
                        tvMobile?.text = customer.mobileNumber ?: "No Number"
                        tvAddress?.text = if (customer.address.isNullOrEmpty()) "No Address Provided" else customer.address
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<CustomerResponse>, t: Throwable) {
                Log.e("CUSTOMER_PROFILE", "Error: ${t.message}")
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
                        tvStatus?.text = "Status: ${m.status ?: "Pending"}"
                        updateDisplayLabelsAndValues(type, m.length ?: "", m.chest ?: "", m.waist ?: "", m.collar ?: "", m.shoulder ?: "", m.sleeve ?: "", m.hip ?: "", m.rise ?: "")
                        
                        val statusLower = m.status?.lowercase() ?: "pending"
                        when {
                            statusLower == "completed" || statusLower == "ready" -> {
                                tvStatus?.setTextColor(ContextCompat.getColor(this@CustomerProfileActivity, R.color.success_text))
                                tvStatus?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@CustomerProfileActivity, R.color.success_bg))
                                layoutOrderActions?.visibility = View.GONE
                            }
                            statusLower == "in progress" -> {
                                tvStatus?.setTextColor(Color.parseColor("#3B82F6"))
                                tvStatus?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
                                layoutOrderActions?.visibility = View.VISIBLE
                            }
                            else -> {
                                tvStatus?.setTextColor(ContextCompat.getColor(this@CustomerProfileActivity, R.color.orange_primary))
                                tvStatus?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF7ED"))
                                layoutOrderActions?.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        handleNoMeasurements(type)
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<MeasurementResponse>, t: Throwable) {
                handleNoMeasurements(type)
            }
        })
    }

    private fun handleNoMeasurements(type: String) {
        tvUnifiedTitle?.text = "$type (0)"
        hideAllContainers()
        layoutOrderActions?.visibility = View.GONE
    }

    private fun updateOrderStatus(newStatus: String) {
        val data = mapOf("mobile_number" to currentMobile, "garment_type" to selectedGarment, "status" to newStatus)
        RetrofitClient.instance.updateOrderStatus(data).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) loadLatestMeasurements(selectedGarment)
            }
            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {}
        })
    }

    private fun updateDisplayLabelsAndValues(type: String, vararg m: String) {
        hideAllContainers()
        // Mapping fields
        if (m.size >= 4) {
            setDisplayField(1, "Length", m[0])
            setDisplayField(2, "Chest", m[1])
            setDisplayField(3, "Waist", m[2])
            setDisplayField(4, "Collar", m[3])
        }
    }

    private fun showMeasurementInputBottomSheet(isEdit: Boolean) {
        // Implementation logic
    }

    private fun setDisplayField(index: Int, label: String, value: String) {
        val container = when(index) { 1->container1; 2->container2; 3->container3; 4->container4; else->null }
        val labelTv = when(index) { 1->tvLabel1; 2->tvLabel2; 3->tvLabel3; 4->tvLabel4; else->null }
        val valueTv = when(index) { 1->tvValue1; 2->tvValue2; 3->tvValue3; 4->tvValue4; else->null }
        container?.visibility = View.VISIBLE
        labelTv?.text = label
        valueTv?.text = if (value.isEmpty()) "-" else "$value in"
    }

    private fun hideAllContainers() {
        listOf(container1, container2, container3, container4, container5, container6, container7, container8).forEach { it?.visibility = View.GONE }
    }

    private fun fetchGarmentCounts() {
        // Implementation for counts
    }
}
