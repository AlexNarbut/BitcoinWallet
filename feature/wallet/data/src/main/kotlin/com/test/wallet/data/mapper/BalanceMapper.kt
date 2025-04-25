package com.test.wallet.data.mapper

import com.test.mempoolapi.models.AddressInfoDTO
import test.wallet.api.model.WalletAddressBalanceInfo

internal fun AddressInfoDTO.toBalanceInfo(walletAddress: String): WalletAddressBalanceInfo? {
    if (chainStats == null) return null
    val balance = (chainStats!!.fundedSum ?: 0L) - (chainStats!!.spentSum ?: 0L)
    return WalletAddressBalanceInfo(
        address = walletAddress,
        amountInSat = balance
    )
}