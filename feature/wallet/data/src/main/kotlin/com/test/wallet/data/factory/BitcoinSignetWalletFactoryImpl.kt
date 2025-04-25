package com.test.wallet.data.factory

import com.test.wallet.data.model.wallet.BitcoinSignetWallet
import test.transaction.api.builder.TransactionBuilder
import test.transaction.api.repository.TransactionRepository
import test.wallet.api.factory.BitcoinWalletFactory
import test.wallet.api.model.Wallet
import test.wallet.api.model.WalletProfile
import test.wallet.api.repository.profile.WalletProfileRepository

class BitcoinSignetWalletFactoryImpl(
    private val walletProfileRepository: WalletProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionBuilder: TransactionBuilder
) : BitcoinWalletFactory {
    override fun create(deviceProfile: WalletProfile): Wallet {
        return BitcoinSignetWallet(
            deviceProfile,
            walletProfileRepository,
            transactionRepository,
            transactionBuilder
        )
    }
}