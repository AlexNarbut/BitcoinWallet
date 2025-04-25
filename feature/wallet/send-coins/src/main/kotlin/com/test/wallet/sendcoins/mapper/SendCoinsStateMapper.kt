package com.test.wallet.sendcoins.mapper

import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.asLCEState
import com.test.bitcoinappuikit.lce.content
import com.test.transaction.data.utils.mempoolSatoshiTotBtc
import com.test.transaction.data.utils.tBtcString
import com.test.wallet.sendcoins.logic.SendCoinsWalletState
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletBalanceInfo

private const val M_BTC_DIVIDER = 100_000.0

internal fun getHeaderState(
    addressState: LCEState<String>,
    balanceInfoState: LCEState<WalletBalanceInfo>
): LCEState<SendCoinsWalletState> {
    if (addressState is LCEState.None) return addressState
    if (balanceInfoState is LCEState.None) return balanceInfoState

    if (addressState is LCEState.Error) return addressState
    if (balanceInfoState is LCEState.Error) return balanceInfoState

    if (addressState is LCEState.Loading) return LCEState.Loading
    if (balanceInfoState is LCEState.Loading) return LCEState.Loading
    else {
        val addressContent = addressState.content ?: ""
        val balanceInfoListContent = balanceInfoState.content ?: WalletBalanceInfo.Default
        val addressBalanceContent = balanceInfoListContent.addressBalances
            .firstOrNull { it.address == addressContent }
            ?: WalletAddressBalanceInfo.getDefault(addressContent)

        return SendCoinsWalletState(
            addressContent,
            addressBalanceContent.amountInSat.mempoolSatoshiTotBtc().tBtcString() + " tBTC",
        ).asLCEState()
    }
}
