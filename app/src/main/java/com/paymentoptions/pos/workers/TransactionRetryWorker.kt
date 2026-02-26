package com.paymentoptions.pos.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.google.gson.Gson
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.refund
import com.paymentoptions.pos.services.apiService.endpoints.void
import com.theminesec.lib.dto.transaction.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRetryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get input data
            val transactionJson = inputData.getString(KEY_TRANSACTION) ?: return@withContext Result.failure()
            val operationType = inputData.getString(KEY_OPERATION_TYPE) ?: return@withContext Result.failure()
            val sdkTransactionJson = inputData.getString(KEY_SDK_TRANSACTION)
            val attemptNumber = inputData.getInt(KEY_ATTEMPT_NUMBER, 0)

            AppLogger.debug("TransactionRetryWorker: Starting scheduled retry attempt $attemptNumber for $operationType")

            // Parse transaction
            val transaction = try {
                Gson().fromJson(transactionJson, TransactionListDataRecord::class.java)
            } catch (e: Exception) {
                AppLogger.error("Failed to parse transaction JSON: ${e.message}")
                return@withContext Result.failure()
            }

            // Parse SDK transaction if available
            val sdkTransaction = sdkTransactionJson?.let {
                try {
                    Gson().fromJson(it, Transaction::class.java)
                } catch (e: Exception) {
                    null
                }
            }

            // Attempt the operation
            val success = when (operationType) {
                OPERATION_VOID -> attemptVoid(transaction, sdkTransaction)
                OPERATION_REFUND -> attemptRefund(transaction, sdkTransaction)
                else -> false
            }

            return@withContext if (success) {
                AppLogger.debug("TransactionRetryWorker: Operation succeeded on attempt $attemptNumber")
                Result.success()
            } else {
                AppLogger.warn("TransactionRetryWorker: Operation failed on attempt $attemptNumber")
                // Retry if not at max attempts (24 attempts over 24 hours)
                if (attemptNumber < 24) {
                    Result.retry()
                } else {
                    AppLogger.error("TransactionRetryWorker: Max attempts (24) reached, giving up")
                    Result.failure(
                        Data.Builder()
                            .putString(KEY_FAILURE_REASON, "Max retry attempts reached")
                            .build()
                    )
                }
            }

        } catch (e: Exception) {
            AppLogger.error("TransactionRetryWorker: Unexpected error: ${e.message}", e)
            return@withContext Result.retry()
        }
    }

    private suspend fun attemptVoid(
        transaction: TransactionListDataRecord,
        sdkTransaction: Transaction?
    ): Boolean {
        return try {
            val response = void(
                context = applicationContext,
                transactionId = transaction.uuid,
                merchantId = transaction.DASMID,
                transaction = sdkTransaction!!
            )
            response != null
        } catch (e: Exception) {
            AppLogger.error("Void attempt failed in worker: ${e.message}", e)
            false
        }
    }

    private suspend fun attemptRefund(
        transaction: TransactionListDataRecord,
        sdkTransaction: Transaction?
    ): Boolean {
        return try {
            val response = refund(
                context = applicationContext,
                transactionId = transaction.uuid,
                merchantId = transaction.DASMID,
                transaction = sdkTransaction,
                amount = transaction.amount
            )
            response != null
        } catch (e: Exception) {
            AppLogger.error("Refund attempt failed in worker: ${e.message}", e)
            false
        }
    }

    companion object {
        const val KEY_TRANSACTION = "transaction"
        const val KEY_SDK_TRANSACTION = "sdk_transaction"
        const val KEY_OPERATION_TYPE = "operation_type"
        const val KEY_ATTEMPT_NUMBER = "attempt_number"
        const val KEY_FAILURE_REASON = "failure_reason"

        const val OPERATION_VOID = "VOID"
        const val OPERATION_REFUND = "REFUND"

        const val WORK_NAME_PREFIX = "transaction_retry"
    }
}

