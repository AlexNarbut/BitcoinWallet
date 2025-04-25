package com.test.wallet.currentstate.logic

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.test.bitcoinappuikit.lce.LCEState
import test.transaction.api.model.TransactionType

@Immutable
data class CryptoScreenState(
    val header: LCEState<CurrentStateHeader> = LCEState.None,
    val transactions: LCEState<List<TransactionHistoryInfoViewModel>> = LCEState.None,
    val isRefreshing: Boolean = false,
)

@Immutable
data class CurrentStateHeader(
    val currentAddress: String,
    val addressBalanceIntBtc: String,
    val fullWalletBalanceIntBtc: String,
)

@Immutable
data class TransactionHistoryInfoViewModel(
    val transactionId: String,
    val type: TransactionType,
    val timeString: String?,
    val amountIntBtc: String,
    val indicatorColor: Color,
    val informationUrl: String?,
)
