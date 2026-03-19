package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val customerDao = db.customerDao()
            
            // 1. Get all customers for backup (Offline-first approach: backup everything)
            // or 1. Get unsynced data
            val unsyncedCustomers = customerDao.getUnsyncedCustomers()
            
            if (unsyncedCustomers.isEmpty()) {
                Log.d("SYNC_WORKER", "No new data to sync")
                return@withContext Result.success()
            }

            // 2. Convert to JSON and Upload to Drive
            val driveHelper = GoogleDriveHelper(applicationContext)
            val allCustomers = customerDao.getAll() // Full backup
            val success = driveHelper.uploadBackup(allCustomers)

            if (success) {
                // 3. Mark as synced in Room
                unsyncedCustomers.forEach {
                    val syncedCustomer = it.copy(isSynced = true)
                    customerDao.update(syncedCustomer)
                }
                Log.d("SYNC_WORKER", "Sync successful")
                Result.success()
            } else {
                Log.e("SYNC_WORKER", "Drive upload failed")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Sync failed: ${e.message}")
            Result.failure()
        }
    }
}
