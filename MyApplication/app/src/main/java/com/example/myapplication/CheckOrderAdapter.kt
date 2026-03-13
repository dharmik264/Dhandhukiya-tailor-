package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CheckOrderModel(
    val id: String,
    val customerName: String,
    val mobile: String,
    val garmentType: String,
    val orderDate: String = "",
    val status: String = ""
)

class CheckOrderAdapter(
    private var orders: List<CheckOrderModel>,
    private val onItemClick: (CheckOrderModel) -> Unit = {}
) : RecyclerView.Adapter<CheckOrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCustomerName: TextView = view.findViewById(R.id.tvCheckCustomerName)
        val tvMobile: TextView = view.findViewById(R.id.tvCheckMobile)
        val tvDate: TextView = view.findViewById(R.id.tvCheckDate)
        val tvGarmentType: TextView = view.findViewById(R.id.tvCheckStatus)
        val tvInitials: TextView = view.findViewById(R.id.tvCheckInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_check_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]

        val name = order.customerName.ifEmpty { "Unknown" }
        holder.tvCustomerName.text = name
        holder.tvMobile.text = order.mobile.ifEmpty { "No number" }
        holder.tvDate.text = order.orderDate.ifEmpty { "" }
        holder.tvGarmentType.text = order.garmentType.ifEmpty { "General" }

        // Generate initials from name
        val initials = name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .take(2)
            .uppercase()
        holder.tvInitials.text = initials.ifEmpty { "?" }

        holder.tvGarmentType.setBackgroundResource(R.drawable.bg_black_rounded)
        holder.tvGarmentType.backgroundTintList =
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1AFF0000"))
        holder.tvGarmentType.setTextColor(android.graphics.Color.parseColor("#FF0000"))

        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<CheckOrderModel>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}