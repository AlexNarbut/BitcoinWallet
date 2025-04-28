package com.test.wallet.data.model.wallet

import com.test.common.response.Response
import com.test.commonextens.Logger
import com.test.commonextens.response.alsoIfError
import com.test.commonextens.response.asGeneralError
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.getValueIfSuccess
import com.test.commonextens.response.mapValueIfSuccess
import com.test.wallet.data.mapper.toHistory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import test.transaction.api.builder.TransactionBuilder
import test.transaction.api.model.TransactionHistoryInfo
import test.transaction.api.model.TransactionSendResult
import test.transaction.api.repository.TransactionRepository
import test.transaction.api.repository.param.GetTransactionsRequest
import test.transaction.api.repository.param.SendTransactionRequest
import test.wallet.api.exception.WalletAddressBalanceNotFoundException
import test.wallet.api.exception.WalletAddressNotSelectedException
import test.wallet.api.model.Wallet
import test.wallet.api.model.WalletAddressInfo
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletBalanceInfo
import test.wallet.api.model.WalletProfile
import test.wallet.api.repository.profile.WalletProfileRepository

class BitcoinSignetWallet(
    override val walletProfile: WalletProfile,
    private val walletProfileRepository: WalletProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionBuilder: TransactionBuilder
) : Wallet {

    private val errorHandler = CoroutineExceptionHandler { context, exception ->
        Logger.e(exception, TAG) { exception.stackTraceToString() }
    }
    private val walletScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    private val _currentAddressState = MutableStateFlow<Response<WalletAddressInfo>>(
        WalletAddressNotSelectedException().asGeneralError()
    )

    override suspend fun init(): Response<Unit> {
        initFirstAddress()
        return Unit.asResponse()
    }

    private suspend fun initFirstAddress() {
        getCurrentWalletAddress().alsoIfError {
            val firstAddress = walletProfile.availableAddress.firstOrNull()
            if (firstAddress != null) {
                setCurrentWalletAddress(firstAddress.address)
            }
        }
    }

    override suspend fun setCurrentWalletAddress(address: String): Response<Unit> {
        val profile = walletProfile.availableAddress.firstOrNull { it.address == address }
            ?: return Response.Error.General(
                WalletAddressBalanceNotFoundException()
            )
        _currentAddressState.update { profile.asResponse() }
        return Unit.asResponse()
    }

    override suspend fun getCurrentWalletAddress(): Response<String> {
        return when (val state = _currentAddressState.value) {
            is Response.Success -> state.value.address.asResponse()
            is Response.Error -> state
        }
    }

    override fun getWalletAddressFlow(): Flow<Response<String>> =
        _currentAddressState.map { address -> address.mapValueIfSuccess { it.address } }

    override suspend fun getAvailableWalletAddresses(): Response<List<String>> {
        return walletProfile.availableAddress.map { it.address }.asResponse()
    }

    override suspend fun getWalletBalance(): Response<WalletBalanceInfo> {
        return coroutineScope {
            val addressBalances = walletProfile.availableAddress
                .map { address ->
                    async { getAddressBalance(address.address) }
                }
                .awaitAll()
                .mapNotNull { balance -> balance.getValueIfSuccess() }
            WalletBalanceInfo(
                addressBalances = addressBalances,
                fullAmountInSat = addressBalances.sumOf { it.amountInSat }
            ).asResponse()
        }
    }

    override suspend fun getAddressBalance(address: String): Response<WalletAddressBalanceInfo> {
        return walletProfileRepository.getAddressBalance(address)
    }

    override suspend fun getTransactionHistory(): Response<List<TransactionHistoryInfo>> {
        val allAddress = walletProfile.availableAddress.map { it.address }
        return when (val state = _currentAddressState.value) {
            is Response.Success -> {
                transactionRepository.getAddressTransaction(GetTransactionsRequest(state.value.address))
                    .mapValueIfSuccess { transactionList ->
                        transactionList.mapNotNull { it.toHistory(allAddress) }
                    }
            }

            is Response.Error -> Response.Error.General(
                WalletAddressBalanceNotFoundException(state.exception, state.message)
            )
        }
    }

    override suspend fun sendCoins(
        destinationAddress: String,
        amount: Long,
        neededFeeAmount: Long?
    ): Response<TransactionSendResult> {
        return when (val state = _currentAddressState.value) {
            is Response.Success -> {
                sendCoins(
                    state.value.address,
                    state.value.primaryKey,
                    destinationAddress,
                    amount,
                    neededFeeAmount
                )
            }

            is Response.Error -> Response.Error.General(
                WalletAddressBalanceNotFoundException(state.exception, state.message)
            )
        }
    }

    private suspend fun sendCoins(
        walletAddress: String,
        key: String,
        destinationAddress: String,
        amount: Long,
        neededFeeAmount: Long?
    ): Response<TransactionSendResult> {
        return transactionBuilder.createSendTransactionHex(
            walletAddress = walletAddress,
            primaryKey = key,
            destinationAddress = destinationAddress,
            amount = amount,
            neededFeeAmount = neededFeeAmount
        ).mapValueIfSuccess { hex ->
            return transactionRepository.send(SendTransactionRequest(hex))
        }
    }

    override suspend fun release(): Response<Unit> {
        walletScope.coroutineContext.cancelChildren()
        return Unit.asResponse()
    }

    companion object {
        private const val TAG = "BitcoinSignetWallet"
    }
}