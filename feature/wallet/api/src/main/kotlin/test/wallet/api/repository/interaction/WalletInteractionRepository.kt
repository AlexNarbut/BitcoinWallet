package test.wallet.api.repository.interaction

import com.test.common.response.Response
import kotlinx.coroutines.flow.Flow
import test.transaction.api.model.TransactionHistoryInfo
import test.transaction.api.model.TransactionSendResult
import test.wallet.api.model.Wallet
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletBalanceInfo
import test.wallet.api.model.WalletProfile

interface WalletInteractionRepository {
    suspend fun init(needRelease : Boolean): Response<Wallet>

    suspend fun getProfile(): Response<WalletProfile>

    suspend fun setCurrentWalletAddress(address: String): Response<Unit>
    suspend fun getCurrentWalletAddress(): Response<String>
    suspend fun getCurrentWalletAddressFlow(): Flow<Response<String>>

    suspend fun getAvailableWalletAddresses(): Response<List<String>>

    suspend fun getWalletBalance(): Response<WalletBalanceInfo>
    suspend fun getAddressBalance(address: String): Response<WalletAddressBalanceInfo>

    suspend fun getTransactionHistory(): Response<List<TransactionHistoryInfo>>

    suspend fun sendCoins(
        destinationAddress: String,
        amount: Long,
        neededFeeAmount: Long? = null
    ): Response<TransactionSendResult>

    suspend fun release(): Response<Unit>
}