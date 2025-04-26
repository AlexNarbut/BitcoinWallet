package com.test.wallet.currentstate.mapper

import androidx.compose.ui.graphics.Color
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.asLCEState
import com.test.bitcoinappuikit.lce.content
import com.test.common.utls.shortCryptoAddress
import com.test.transaction.data.utils.mempoolSatoshiTotBtc
import com.test.transaction.data.utils.tBtcString
import com.test.wallet.currentstate.logic.CurrentStateHeader
import com.test.wallet.currentstate.logic.TransactionHistoryInfoViewModel
import test.transaction.api.model.TransactionHistoryInfo
import test.transaction.api.model.TransactionType
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletBalanceInfo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale


internal fun getHeaderState(
    addressState: LCEState<String>,
    balanceInfoState: LCEState<WalletBalanceInfo>
): LCEState<CurrentStateHeader> {
    if (addressState is LCEState.Error) return addressState
    if (balanceInfoState is LCEState.Error) return balanceInfoState

    if (addressState is LCEState.None) return addressState
    if (balanceInfoState is LCEState.None) return balanceInfoState

    if (addressState is LCEState.Loading) return LCEState.Loading
    if (balanceInfoState is LCEState.Loading) return LCEState.Loading
    else {
        val addressContent = addressState.content ?: ""
        val balanceInfoListContent = balanceInfoState.content ?: WalletBalanceInfo.Default
        val addressBalanceContent = balanceInfoListContent.addressBalances
            .firstOrNull { it.address == addressContent }
            ?: WalletAddressBalanceInfo.getDefault(addressContent)

        return CurrentStateHeader(
            addressContent,
            addressBalanceContent.amountInSat.mempoolSatoshiTotBtc().tBtcString()+ " tBTC",
            balanceInfoListContent.fullAmountInSat.mempoolSatoshiTotBtc().tBtcString()+ " tBTC"
        ).asLCEState()
    }
}

internal fun TransactionHistoryInfo.toViewModel(): TransactionHistoryInfoViewModel {
    return TransactionHistoryInfoViewModel(
        transactionId = transactionInfo.id.shortCryptoAddress(),
        type = transactionType,
        timeString = transactionInfo.blockTime?.let { formatter.format(it)},
        amountIntBtc = amountInSat.mempoolSatoshiTotBtc().tBtcString()+ " tBTC",
        indicatorColor = transactionType.toIndicatorColor(),
        informationUrl = transactionInfo.informationUrl
    )
}

private val formatter: DateFormat = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.US)



internal fun TransactionType.toIndicatorColor(): Color = when (this) {
    TransactionType.UNKNOWN -> Color.Gray.copy(alpha = 0.5f)
    TransactionType.INCOME -> Color.Green
    TransactionType.EXPENSE -> Color.Gray
    TransactionType.SELF_TRANSFER -> Color.Gray
}