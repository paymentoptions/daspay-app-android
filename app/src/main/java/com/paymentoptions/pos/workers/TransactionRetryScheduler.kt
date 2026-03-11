package com.paymentoptions.pos.workers

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.theminesec.lib.dto.transaction.Transaction
import java.util.concurrent.TimeUnit

object TransactionRetryScheduler {

    /**
     * Schedule hourly retries for a failed transaction operation
     * Will retry every 1 hour for up to 24 hours
     *
     * @param context Android context
     * @param transaction The transaction to retry
     * @param sdkTransaction The SDK transaction object (for SOFTPOS operations)
     * @param operationType Either VOID or REFUND
     */
    fun scheduleHourlyRetries(
        context: Context,
        transaction: TransactionListDataRecord,
        sdkTransaction: Transaction?,
        operationType: String
    ) {
        try {
            val transactionJson = Gson().toJson(transaction)
            val sdkTransactionJson = sdkTransaction?.let { Gson().toJson(it) }

            AppLogger.debug("Scheduling hourly retries for $operationType transaction: ${transaction.uuid}")

            // Create input data
            val inputData = Data.Builder()
                .putString(TransactionRetryWorker.KEY_TRANSACTION, transactionJson)
                .putString(TransactionRetryWorker.KEY_SDK_TRANSACTION, sdkTransactionJson)
                .putString(TransactionRetryWorker.KEY_OPERATION_TYPE, operationType)
                .putInt(TransactionRetryWorker.KEY_ATTEMPT_NUMBER, 1)
                .build()

            // Create constraints - require network
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Create periodic work request - runs every 1 hour
            val workRequest = PeriodicWorkRequestBuilder<TransactionRetryWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 15, // Can run within 15 minutes of scheduled time
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    15, // Wait 15 minutes if work fails before retrying
                    TimeUnit.MINUTES
                )
                .addTag(TAG_TRANSACTION_RETRY)
                .addTag("${TAG_TRANSACTION_RETRY}_${transaction.uuid}")
                .build()

            // Enqueue the work with unique name to avoid duplicates
            val workName = "${TransactionRetryWorker.WORK_NAME_PREFIX}_${transaction.uuid}_${operationType}"

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    workName,
                    ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                    workRequest
                )

            AppLogger.debug("Successfully scheduled hourly retries with work name: $workName")

            // Also schedule a one-time work to cancel this after 24 hours
            scheduleCleanupTask(context, workName, transaction.uuid)

        } catch (e: Exception) {
            AppLogger.error("Failed to schedule hourly retries: ${e.message}", e)
        }
    }

    /**
     * Schedule a one-time task to cancel the periodic retries after 24 hours
     */
    private fun scheduleCleanupTask(context: Context, workName: String, transactionId: String) {
        val cleanupData = Data.Builder()
            .putString(KEY_WORK_NAME_TO_CANCEL, workName)
            .putString(KEY_TRANSACTION_ID, transactionId)
            .build()

        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupRetryWorker>()
            .setInputData(cleanupData)
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueue(cleanupRequest)

        AppLogger.debug("Scheduled cleanup task for $workName after 24 hours")
    }

    /**
     * Cancel scheduled retries for a specific transaction
     */
    fun cancelRetries(context: Context, transactionId: String) {
        try {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag("${TAG_TRANSACTION_RETRY}_${transactionId}")

            AppLogger.debug("Cancelled all retry work for transaction: $transactionId")
        } catch (e: Exception) {
            AppLogger.error("Failed to cancel retries: ${e.message}", e)
        }
    }

    /**
     * Cancel all scheduled transaction retries
     */
    fun cancelAllRetries(context: Context) {
        try {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(TAG_TRANSACTION_RETRY)

            AppLogger.debug("Cancelled all transaction retry work")
        } catch (e: Exception) {
            AppLogger.error("Failed to cancel all retries: ${e.message}", e)
        }
    }

    /**
     * Get retry status for a transaction
     */
    fun getRetryStatus(context: Context, transactionId: String) {
        try {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosByTag("${TAG_TRANSACTION_RETRY}_${transactionId}")
                .get()

            workInfos.forEach { workInfo ->
                AppLogger.debug("Retry work for $transactionId: ${workInfo.state}, run attempt: ${workInfo.runAttemptCount}")
            }
        } catch (e: Exception) {
            AppLogger.error("Failed to get retry status: ${e.message}", e)
        }
    }

    private const val TAG_TRANSACTION_RETRY = "transaction_retry"
    private const val KEY_WORK_NAME_TO_CANCEL = "work_name_to_cancel"
    private const val KEY_TRANSACTION_ID = "transaction_id"
}

/**
 * Worker to cleanup/cancel periodic retries after 24 hours
 */
class CleanupRetryWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val workName = inputData.getString(TransactionRetryScheduler::class.java.getDeclaredField("KEY_WORK_NAME_TO_CANCEL").get(null) as String)
        val transactionId = inputData.getString(TransactionRetryScheduler::class.java.getDeclaredField("KEY_TRANSACTION_ID").get(null) as String)

        if (workName != null) {
            WorkManager.getInstance(applicationContext)
                .cancelUniqueWork(workName)

            AppLogger.debug("Cleanup: Cancelled retry work after 24 hours: $workName")
        }

        if (transactionId != null) {
            AppLogger.debug("Cleanup: Transaction $transactionId retry period expired after 24 hours")
        }

        return Result.success()
    }
}

