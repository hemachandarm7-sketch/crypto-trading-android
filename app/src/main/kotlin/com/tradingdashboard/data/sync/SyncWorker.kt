package com.tradingdashboard.data.sync

import android.content.Context
import androidx.work.*
import com.tradingdashboard.data.repository.TradeRepository
import com.tradingdashboard.util.Constants
import com.tradingdashboard.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager job for background trade sync
 * Syncs local changes with Supabase periodically
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var tradeRepository: TradeRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Logger.section("SyncWorker: Starting background sync")
            
            val userId = inputData.getString(KEY_USER_ID) 
                ?: Constants.User.DEFAULT_USER_ID

            Logger.d("Syncing trades for user: $userId", tag = "SyncWorker")

            // Perform full sync
            val result = tradeRepository.fullSync(userId)

            result
                .onSuccess { syncResult ->
                    Logger.d(
                        "Sync successful: $syncResult",
                        tag = "SyncWorker"
                    )
                    Logger.logSyncOperation("Background Sync", success = true)
                }
                .onError { error ->
                    Logger.e(
                        "Sync failed: ${error.message}",
                        tag = "SyncWorker",
                        throwable = error
                    )
                    Logger.logSyncOperation("Background Sync", success = false)
                    
                    // Retry on network errors
                    if (error.message?.contains("network", ignoreCase = true) == true) {
                        return@onError Result.retry()
                    }
                }

            // Return success if sync succeeded
            if (result.isSuccess) {
                Logger.section("SyncWorker: Completed successfully")
                Result.success()
            } else {
                Logger.section("SyncWorker: Failed, will retry")
                Result.retry()
            }
        } catch (e: Exception) {
            Logger.e("Unexpected error in SyncWorker", tag = "SyncWorker", throwable = e)
            Logger.section("SyncWorker: Error - retrying")
            Result.retry()
        }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val SYNC_WORK_TAG = "trade_sync"
        private const val SYNC_WORK_NAME = "trade_background_sync"
        
        /**
         * Enqueue periodic background sync
         */
        fun enqueuePeriodicSync(
            context: Context,
            userId: String = Constants.User.DEFAULT_USER_ID
        ) {
            Logger.d("Enqueuing periodic sync for user: $userId", tag = "SyncWorker")
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = Constants.Sync.AUTO_SYNC_INTERVAL_MINUTES.toLong(),
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 5,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setInputData(
                    workDataOf(KEY_USER_ID to userId)
                )
                .addTag(SYNC_WORK_TAG)
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    initialBackoff = 15,
                    backoffUnit = TimeUnit.MINUTES
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            
            Logger.d("Periodic sync enqueued successfully", tag = "SyncWorker")
        }

        /**
         * Enqueue one-time sync (immediate)
         */
        fun enqueueSyncNow(
            context: Context,
            userId: String = Constants.User.DEFAULT_USER_ID
        ) {
            Logger.d("Enqueuing immediate sync for user: $userId", tag = "SyncWorker")
            
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(
                    workDataOf(KEY_USER_ID to userId)
                )
                .addTag(SYNC_WORK_TAG)
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    initialBackoff = 5,
                    backoffUnit = TimeUnit.SECONDS
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${SYNC_WORK_NAME}_now",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
            
            Logger.d("Immediate sync enqueued successfully", tag = "SyncWorker")
        }

        /**
         * Cancel all pending sync tasks
         */
        fun cancelSync(context: Context) {
            Logger.d("Canceling all sync tasks", tag = "SyncWorker")
            WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_WORK_TAG)
        }

        /**
         * Observe sync work status
         */
        fun observeSyncStatus(context: Context): androidx.lifecycle.LiveData<WorkInfo> {
            return WorkManager.getInstance(context)
                .getWorkInfosForTagLiveData(SYNC_WORK_TAG)[0]
        }
    }
}

/**
 * Sync Manager - handles sync lifecycle and coordination
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tradeRepository: TradeRepository
) {
    
    private var lastSyncTime: Long = 0
    private var isSyncing: Boolean = false

    /**
     * Initialize sync manager and set up periodic sync
     */
    fun initialize(userId: String = Constants.User.DEFAULT_USER_ID) {
        Logger.d("Initializing SyncManager", tag = "SyncManager")
        
        // Set up periodic background sync
        SyncWorker.enqueuePeriodicSync(context, userId)
        
        Logger.d("SyncManager initialized", tag = "SyncManager")
    }

    /**
     * Request immediate sync
     */
    suspend fun syncNow(userId: String = Constants.User.DEFAULT_USER_ID) {
        // Prevent overlapping syncs
        if (isSyncing) {
            Logger.d("Sync already in progress, skipping", tag = "SyncManager")
            return
        }

        isSyncing = true
        val syncStart = System.currentTimeMillis()
        Logger.section("Manual Sync Requested")

        try {
            val result = tradeRepository.fullSync(userId)
            
            result
                .onSuccess {
                    lastSyncTime = System.currentTimeMillis()
                    Logger.d(
                        "Sync completed in ${System.currentTimeMillis() - syncStart}ms",
                        tag = "SyncManager"
                    )
                }
                .onError { error ->
                    Logger.e("Sync failed: ${error.message}", tag = "SyncManager", throwable = error)
                }
        } finally {
            isSyncing = false
            Logger.section("Manual Sync Completed")
        }
    }

    /**
     * Request immediate sync via WorkManager (for UI feedback)
     */
    fun syncNowBackground(userId: String = Constants.User.DEFAULT_USER_ID) {
        Logger.d("Requesting background sync", tag = "SyncManager")
        SyncWorker.enqueueSyncNow(context, userId)
    }

    /**
     * Get time since last sync
     */
    fun getLastSyncTime(): Long = lastSyncTime

    /**
     * Check if sync is currently running
     */
    fun isSyncInProgress(): Boolean = isSyncing

    /**
     * Get unsynced trade count
     */
    suspend fun getUnsyncedCount(userId: String): Int {
        return tradeRepository.getUnsyncedCount(userId)
    }

    /**
     * Stop all sync operations
     */
    fun stopSync() {
        Logger.d("Stopping all sync operations", tag = "SyncManager")
        SyncWorker.cancelSync(context)
    }

    /**
     * Get sync status for UI display
     */
    fun getSyncStatus(): SyncStatus {
        return SyncStatus(
            isSyncing = isSyncing,
            lastSyncTime = lastSyncTime,
            isConnected = true  // TODO: Check actual connectivity
        )
    }
}

/**
 * Sync status for UI
 */
data class SyncStatus(
    val isSyncing: Boolean,
    val lastSyncTime: Long,
    val isConnected: Boolean
) {
    fun getStatusText(): String = when {
        isSyncing -> "Syncing..."
        lastSyncTime == 0L -> "Never synced"
        else -> {
            val now = System.currentTimeMillis()
            val diff = now - lastSyncTime
            when {
                diff < 60_000 -> "Synced just now"
                diff < 3_600_000 -> "Synced ${diff / 60_000} min ago"
                diff < 86_400_000 -> "Synced ${diff / 3_600_000} hrs ago"
                else -> "Synced ${diff / 86_400_000} days ago"
            }
        }
    }
}
