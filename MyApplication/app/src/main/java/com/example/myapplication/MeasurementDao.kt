package com.example.myapplication

import androidx.room.*

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE customerMobile = :mobile AND garmentType = :type LIMIT 1")
    suspend fun getMeasurement(mobile: String, type: String): Measurement?

    @Query("SELECT * FROM measurements")
    suspend fun getAllMeasurements(): List<Measurement>

    @Query("SELECT * FROM measurements ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentMeasurements(limit: Int): List<Measurement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: Measurement)

    @Update
    suspend fun update(measurement: Measurement)

    @Delete
    suspend fun delete(measurement: Measurement)
}
