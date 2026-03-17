package com.example.myapplication

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object ResponsiveUtils {

    /**
     * Automatically sets the most appropriate LayoutManager for a RecyclerView
     * based on screen width and item size.
     */
    fun setupResponsiveRecyclerView(context: Context, recyclerView: RecyclerView, itemWidthDp: Int = 180) {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        
        val spanCount = (dpWidth / itemWidthDp).toInt().coerceAtLeast(1)
        
        if (spanCount > 1) {
            recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Returns the recommended span count for a grid based on screen width.
     */
    fun getDynamicSpanCount(context: Context, itemWidthDp: Int = 180): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / itemWidthDp).toInt().coerceAtLeast(1)
    }

    /**
     * Returns true if the device is a tablet.
     */
    fun isTablet(context: Context): Boolean {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return dpWidth >= 600
    }
}
