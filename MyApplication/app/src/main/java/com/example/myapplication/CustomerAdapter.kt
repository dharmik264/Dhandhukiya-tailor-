package com.example.myapplication

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

// Data model for customer list items with status
data class CustomerDisplayModel(
    val id: String,
    val name: String,
    val mobile: String,
    val length: String,
    val status: String = "Pending"
)

class CustomerAdapter(
    private var customers: List<CustomerDisplayModel>,
    private val onItemClick: (CustomerDisplayModel) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvMobile: TextView = view.findViewById(R.id.tvMobile)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]
        holder.tvName.text = customer.name.ifEmpty { "Unknown Customer" }
        holder.tvMobile.text = customer.mobile.ifEmpty { "No Number" }
        
        // Status Binding logic
        val status = customer.status.ifEmpty { "Pending" }
        holder.tvStatusBadge.text = status
        val statusColor = when (status.lowercase()) {
            "pending" -> Color.parseColor("#FFA500")      // Orange
            "in progress" -> Color.parseColor("#2196F3") // Blue
            "ready" -> Color.parseColor("#4CAF50")       // Green
            else -> Color.GRAY
        }
        
        holder.tvStatusBadge.setTextColor(statusColor)
        holder.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(statusColor).withAlpha(26) // ~10% opacity

        holder.itemView.setOnClickListener {
            onItemClick(customer)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(customer.mobile)
        }
    }

    override fun getItemCount() = customers.size

    fun updateData(newCustomers: List<CustomerDisplayModel>) {
        customers = newCustomers
        notifyDataSetChanged()
    }
}