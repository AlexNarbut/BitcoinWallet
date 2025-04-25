@file:OptIn(ExperimentalAtomicApi::class, ExperimentalAtomicApi::class)

package com.test.wallet.data.model.wallet

import com.test.common.response.Response
import com.test.commonextens.response.alsoIfError
import com.test.commonextens.response.alsoIfSuccess
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.getValueIfSuccess
import com.test.commonextens.response.mapValueIfSuccess
import com.test.wallet.data.mapper.toHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import test.transaction.api.builder.TransactionBuilder
import test.transaction.api.model.TransactionHistoryInfo
import test.transaction.api.model.TransactionSendResult
import test.transaction.api.repository.TransactionRepository
import test.transaction.api.repository.param.GetTransactionsRequest
import test.transaction.api.repository.param.SendTransactionRequest
import test.wallet.api.exception.WalletAddressBalanceNotFoundException
import test.wallet.api.model.Wallet
import test.wallet.api.model.WalletAddressInfo
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletBalanceInfo
import test.wallet.api.model.WalletProfile
import test.wallet.api.repository.profile.WalletProfileRepository
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class BitcoinSignetWallet(
    override val walletProfile: WalletProfile,
    private val walletProfileRepository: WalletProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionBuilder: TransactionBuilder
) : Wallet {

    private val walletScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val currentAddress = AtomicReference<WalletAddressInfo?>(null)

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
        currentAddress.store(profile)
        return Unit.asResponse()
    }

    override suspend fun getCurrentWalletAddress(): Response<String> {
        return currentAddress.load()?.address
            ?.asResponse()
            ?: Response.Error.General(
                WalletAddressBalanceNotFoundException()
            )
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
        return currentAddress.load()?.address
            ?.let {
                transactionRepository.getAddressTransaction(GetTransactionsRequest(it))
                    .mapValueIfSuccess { transactionList ->
                        transactionList.mapNotNull { it.toHistory(allAddress) }
                    }
            }
            ?: Response.Error.General(
                WalletAddressBalanceNotFoundException()
            )
    }

    override suspend fun sendCoins(
        destinationAddress: String,
        amount: Long,
        feeAmount: Long
    ): Response<TransactionSendResult> {
        return currentAddress.load()
            ?.let { address ->
                sendCoins(
                    address.address,
                    address.primaryKey,
                    destinationAddress,
                    amount,
                    feeAmount
                )
            }
            ?: Response.Error.General(
                WalletAddressBalanceNotFoundException()
            )
    }

    private suspend fun sendCoins(
        walletAddress: String,
        key: String,
        destinationAddress: String,
        amount: Long,
        feeAmount: Long
    ): Response<TransactionSendResult> {
        return transactionBuilder.createSendTransactionHex(
            walletAddress = walletAddress,
            primaryKey = key,
            destinationAddress = destinationAddress,
            amount = amount,
            feeAmount = feeAmount
        ).mapValueIfSuccess { hex ->
            return transactionRepository.send(SendTransactionRequest(hex))
        }
    }

    override suspend fun release(): Response<Unit> {
        walletScope.coroutineContext.cancelChildren()
        return Unit.asResponse()
    }
}