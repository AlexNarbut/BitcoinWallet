package test.transaction.api.builder

import com.test.common.response.Response

interface TransactionBuilder {
    suspend fun createSendTransactionHex(
        walletAddress: String,
        primaryKey: String,
        destinationAddress: String,
        amount: Long,
        feeAmount: Long
    ): Response<String>
}