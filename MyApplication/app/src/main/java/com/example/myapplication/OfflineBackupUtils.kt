package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object OfflineBackupUtils {

    /**
     * Generates a local CSV file in the Downloads folder.
     */
    suspend fun exportDataToCSV(context: Context) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Starting Backup...", Toast.LENGTH_SHORT).show()
        }
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val customers = db.customerDao().getAll()
                val measurements = db.measurementDao().getAllMeasurements()

                if (customers.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No data to export.", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val csvHeader = "Customer Name,Mobile Number,Address,Garment Type,Length,Chest,Waist,Collar,Shoulder,Sleeve,Hip,Rise,Notes,Status\n"
                val csvContent = StringBuilder(csvHeader)

                for (customer in customers) {
                    val custMeasurements = measurements.filter { it.customerMobile == customer.mobileNumber }
                    val name = customer.name.ifEmpty { "${customer.firstName} ${customer.lastName}".trim() }
                    
                    if (custMeasurements.isEmpty()) {
                        csvContent.append("\"$name\",\"${customer.mobileNumber}\",\"${customer.address}\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"\n")
                    } else {
                        for (m in custMeasurements) {
                            csvContent.append("\"$name\",\"${customer.mobileNumber}\",\"${customer.address}\",")
                            csvContent.append("\"${m.garmentType}\",\"${m.length}\",\"${m.chest}\",\"${m.waist}\",")
                            csvContent.append("\"${m.collar}\",\"${m.shoulder}\",\"${m.sleeve}\",\"${m.hip}\",\"${m.rise}\",")
                            csvContent.append("\"${m.notes}\",\"${m.status}\"\n")
                        }
                    }
                }

                saveCSVToFile(context, csvContent.toString())

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun saveCSVToFile(context: Context, content: String) {
        val fileName = "TailorHub_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/comma-separated-values")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(content)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!folder.exists()) folder.mkdirs()
                val file = java.io.File(folder, fileName)
                file.writeText(content)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup saved to Downloads", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "File Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
