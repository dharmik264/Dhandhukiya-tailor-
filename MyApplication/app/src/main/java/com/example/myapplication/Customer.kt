package com.example.myapplication

import androidx.room.*
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "customers",
    indices = [Index(value = ["mobileNumber"], unique = true)]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val firstName: String = "",
    val lastName: String = "",
    val mobileNumber: String = "",
    val username: String = "",
    val password: String = "",
    val name: String = "",
    val address: String = "",
    val length: String = "",
    val status: String = "Pending",
    
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
