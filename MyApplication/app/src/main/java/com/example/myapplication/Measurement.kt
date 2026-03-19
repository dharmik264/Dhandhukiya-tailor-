package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["mobileNumber"],
            childColumns = ["customerMobile"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerMobile", "garmentType"], unique = true)]
)
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerMobile: String,
    val garmentType: String,
    val length: String = "",
    val chest: String = "",
    val waist: String = "",
    val collar: String = "",
    val shoulder: String = "",
    val sleeve: String = "",
    val hip: String = "",
    val rise: String = "",
    val notes: String = "",
    val status: String = "Pending",
    val updatedAt: Long = System.currentTimeMillis()
)
