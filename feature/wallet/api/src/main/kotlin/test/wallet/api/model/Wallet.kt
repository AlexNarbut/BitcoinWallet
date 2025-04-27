package test.wallet.api.model

import com.test.common.response.Response
import test.transaction.api.model.TransactionHistoryInfo
import test.transaction.api.model.TransactionSendResult

interface Wallet {
    val walletProfile: WalletProfile

    suspend fun init(): Response<Unit>
    suspend fun release(): Response<Unit>

    suspend fun setCurrentWalletAddress(address: String): Response<Unit>
    suspend fun getCurrentWalletAddress(): Response<String>

    suspend fun getWalletBalance(): Response<WalletBalanceInfo>
    suspend fun getAddressBalance(address: String): Response<WalletAddressBalanceInfo>

    suspend fun getTransactionHistory(): Response<List<TransactionHistoryInfo>>

    suspend fun sendCoins(
        destinationAddress: String,
        amount: Long,
        neededFeeAmount: Long?
    ): Response<TransactionSendResult>
}