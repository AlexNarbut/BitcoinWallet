@file:OptIn(ExperimentalAtomicApi::class)

package com.test.wallet.data.repository.interaction

import com.test.common.response.Response
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.safeRun
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import test.wallet.api.repository.interaction.WalletInteractionRepository
import test.wallet.api.repository.profile.WalletProfileRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import test.transaction.api.model.TransactionHistoryInfo
import test.transaction.api.model.TransactionSendResult
import test.wallet.api.exception.WalletProfileNotInitializedException
import test.wallet.api.factory.BitcoinWalletFactory
import test.wallet.api.model.Wallet
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletBalanceInfo
import test.wallet.api.model.WalletProfile
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class WalletInteractionRepositoryImpl(
    private val bitcoinWalletFactory: BitcoinWalletFactory,
    private val profileRepository: WalletProfileRepository,
) : WalletInteractionRepository {
    private val initMutex = Mutex()
    private val currentProfile = AtomicReference<Wallet?>(null)

    override suspend fun init(needRelease: Boolean): Response<Wallet> {
        if (needRelease) release()
        return currentProfile.load()?.asResponse()
            ?: initMutex.withLock {
                return currentProfile.load()?.asResponse()
                    ?: when (val profileInfo = profileRepository.getProfile()) {
                        is Response.Success -> {
                            val wallet = bitcoinWalletFactory.create(profileInfo.value)
                            when (val response = wallet.init()) {
                                is Response.Success -> {
                                    currentProfile.store(wallet)
                                    wallet.asResponse()
                                }

                                is Response.Error -> {
                                    wallet.release()
                                    response
                                }
                            }
                        }

                        is Response.Error -> profileInfo
                    }
            }
    }

    override suspend fun getProfile(): Response<WalletProfile> =
        executeWithProfile { wallet ->
            wallet.walletProfile.asResponse()
        }

    override suspend fun setCurrentWalletAddress(address: String): Response<Unit> =
        executeWithProfile { wallet ->
            wallet.setCurrentWalletAddress(address)
        }

    override suspend fun getCurrentWalletAddress(): Response<String> =
        executeWithProfile { wallet ->
            wallet.getCurrentWalletAddress()
        }

    override suspend fun getCurrentWalletAddressFlow(): Flow<Response<String>> =
        executeFlowWithProfile { wallet ->
            wallet.getWalletAddressFlow()
        }

    override suspend fun getAvailableWalletAddresses(): Response<List<String>> =
        executeWithProfile { wallet ->
            wallet.getAvailableWalletAddresses()
        }

    override suspend fun getWalletBalance(): Response<WalletBalanceInfo> =
        executeWithProfile { wallet ->
            wallet.getWalletBalance()
        }


    override suspend fun getAddressBalance(address: String): Response<WalletAddressBalanceInfo> =
        executeWithProfile { wallet ->
            wallet.getAddressBalance(address)
        }

    override suspend fun getTransactionHistory(): Response<List<TransactionHistoryInfo>> =
        executeWithProfile { wallet ->
            wallet.getTransactionHistory()
        }

    override suspend fun sendCoins(
        destinationAddress: String,
        amount: Long,
        neededFeeAmount: Long?
    ): Response<TransactionSendResult> = executeWithProfile { wallet ->
        wallet.sendCoins(destinationAddress, amount, neededFeeAmount)
    }

    override suspend fun release(): Response<Unit> = initMutex.withLock {
        safeRun {
            currentProfile.load()?.release()
            currentProfile.store(null)
        }
    }

    private suspend inline fun <T> executeWithProfile(block: (Wallet) -> Response<T>): Response<T> {
        val profile = currentProfile.load()
        return if (profile != null) block.invoke(profile)
        else {
            when (val initResponse = init(needRelease = false)) {
                is Response.Success -> {
                    block.invoke(initResponse.value)
                }

                is Response.Error -> {
                    Response.Error.General(
                        WalletProfileNotInitializedException(
                            cause = initResponse.exception,
                            message = "Wallet is not initialized ${initResponse.message?.let { "($it)" } ?: ""}"
                        )
                    )
                }
            }
        }
    }

    private suspend inline fun <T> executeFlowWithProfile(block: (Wallet) -> Flow<Response<T>>): Flow<Response<T>> {
        val profile = currentProfile.load()
        return if (profile != null) block.invoke(profile)
        else {
            when (val initResponse = init(needRelease = false)) {
                is Response.Success -> {
                    block.invoke(initResponse.value)
                }

                is Response.Error -> {
                    flowOf(
                        Response.Error.General(
                            WalletProfileNotInitializedException(
                                cause = initResponse.exception,
                                message = "Wallet is not initialized ${initResponse.message?.let { "($it)" } ?: ""}"
                            )
                        )
                    )
                }
            }
        }
    }
}