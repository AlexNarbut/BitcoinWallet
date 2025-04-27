package com.test.bitcoinapp.exception

import com.test.bitcoinapp.R
import com.test.bitcoinappuikit.string
import com.test.common.exception.ErrorMessageFactory
import test.transaction.api.exception.TransactionException
import test.transaction.api.exception.TransactionNotEnoughCoinsException
import test.transaction.api.exception.TransactionSendErrorException
import test.transaction.api.exception.TransactionUtxoNotFoundException
import test.wallet.api.exception.WalletAddressBalanceNotFoundException
import test.wallet.api.exception.WalletAddressBalanceNotMappedException
import test.wallet.api.exception.WalletProfileException
import test.wallet.api.exception.WalletProfileNotInitializedException
import test.wallet.api.exception.WalletProfileNotLoadFromFileException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject

class AppErrorMessageFactory @Inject constructor() : ErrorMessageFactory {

    override fun create(exception: Throwable?, noMessage: Boolean): String {
        var message: String = when (exception) {
            is WalletProfileException -> {
                when (exception) {
                    is WalletAddressBalanceNotFoundException -> string(R.string.error_wallet_address_balance_not_found)
                    is WalletAddressBalanceNotMappedException -> string(R.string.error_wallet_address_balance_not_mapped)
                    is WalletProfileNotInitializedException -> string(R.string.error_wallet_profile_not_initialized)
                    is WalletProfileNotLoadFromFileException -> string(R.string.error_wallet_profile_not_load_from_file)
                    else -> string(R.string.error_default)
                }
            }

            is TransactionException -> {
                when (exception) {
                    is TransactionNotEnoughCoinsException -> string(
                        R.string.error_transaction_not_enough_coins,
                        exception.inputCoinsInSat,
                        exception.neededCoinsWithFeeInSat
                    )

                    is TransactionUtxoNotFoundException -> string(R.string.error_transaction_utxo_not_found)
                    is TransactionSendErrorException -> string(R.string.error_transaction_send_error)
                    else -> string(R.string.error_default)
                }
            }

            is SocketTimeoutException -> string(R.string.error_network_is_not_available)
            is UnknownHostException -> string(R.string.error_network_is_not_available)
            is UnresolvedAddressException -> string(R.string.error_network_is_not_available)
            else -> string(R.string.error_default)
        }

        if (!exception?.message.isNullOrBlank() && !noMessage) {
            message = message.plus(exception?.message?.let { "(${it})" } ?: "")
        }

        return message
    }
}