package com.test.wallet.sendcoins.logic

import androidx.compose.runtime.Immutable
import com.test.bitcoinappuikit.lce.LCEState

@Immutable
data class SendCoinsScreenState(
    val walletState: LCEState<SendCoinsWalletState> = LCEState.None,
    val sendFormState: SendFormState = SendFormState(),
    val sendingState: SendState = SendState.Default,
)

@Immutable
data class SendCoinsWalletState(
    val currentAddress: String,
    val addressBalanceIntBtc: String,
)

@Immutable
data class SendFormState(
    val sendAddress: EditTextState = EditTextState(),
    val sendAmount: EditTextState = EditTextState(),
    val feeAmount: EditTextState = EditTextState(),
    val isVisible: Boolean = false,
    val canSend: Boolean = false,
)

@Immutable
data class EditTextState(
    val str: String = "",
    val isValid: Boolean = false
)

@Immutable
sealed class SendState {
    data object Default : SendState()
    data object Sending : SendState()
    data class SentSuccess(val shortTransactionId: String, val informationUrl: String) : SendState()
    data class SentError(val errorString: String?) : SendState()
}
