package com.test.wallet.sendcoins.logic

import androidx.lifecycle.viewModelScope
import com.test.bitcoinappuikit.vm.BaseViewModel
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.applyFromExecuting
import com.test.bitcoinappuikit.lce.content
import com.test.bitcoinappuikit.lce.isSuccess
import com.test.common.AppDispatchers
import com.test.common.exception.ErrorMessageFactory
import com.test.common.utls.shortCryptoAddress
import com.test.commonextens.response.alsoIfError
import com.test.commonextens.response.alsoIfSuccess
import com.test.transaction.data.utils.btcToMempoolSatoshi
import com.test.transaction.data.utils.isValidElectrumAddress
import com.test.transaction.data.utils.safeToBigDecimal
import com.test.wallet.sendcoins.mapper.getHeaderState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import test.wallet.api.model.WalletBalanceInfo
import test.wallet.api.repository.interaction.WalletInteractionRepository
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class SendCoinsViewModel @Inject constructor(
    private val appDispatcher: AppDispatchers,
    private val walletInteractionRepository: WalletInteractionRepository,
    private val errorMessageFactory: ErrorMessageFactory
) : BaseViewModel() {

    private val _addressStateFlow = MutableStateFlow<LCEState<String>>(LCEState.None)
    private val _balanceStateFlow = MutableStateFlow<LCEState<WalletBalanceInfo>>(LCEState.None)
    private val _sendFormStateFlow = MutableStateFlow(SendFormState())
    private val _sendStateFlow = MutableStateFlow<SendState>(SendState.Default)

    val screenState: StateFlow<SendCoinsScreenState> = combine(
        _addressStateFlow,
        _balanceStateFlow,
        _sendFormStateFlow,
        _sendStateFlow
    ) { addressState, balance, form, sendState ->
        val headerState = getHeaderState(addressState, balance)

        val currentAddress = addressState.content
        val isAddressValid = isValidElectrumAddress(form.sendAddress.str)
        val isAmountValid = form.sendAmount.str.isAmountValid()
        val isDifferentAddresses = form.sendAddress.str != currentAddress
        val canSend = isAddressValid
                && isAmountValid
                && isDifferentAddresses
                && sendState != SendState.Sending

        SendCoinsScreenState(
            walletState = headerState,
            sendFormState = SendFormState(
                sendAddress = form.sendAddress.copy(isValid = isAddressValid),
                sendAmount = form.sendAmount.copy(isValid = isAmountValid),
                isVisible = headerState.isSuccess(),
                canSend = canSend
            ),
            sendingState = sendState
        )
    }.flowOn(appDispatcher.default)
        .stateIn(viewModelScope, started = SharingStarted.Lazily, SendCoinsScreenState())

    private var updateWalletProfileJob: Job? = null
    private var editorJob: Job? = null
    private var sendJob: Job? = null

    init {
        viewModelScope.launch {
            updateWalletProfile()
        }
    }

    fun updateWalletProfile(reload: Boolean = true) {
        updateWalletProfileJob?.cancel()
        updateWalletProfileJob = safeLaunch(appDispatcher.io) {
            _addressStateFlow.applyFromExecuting(
                execute = {
                    walletInteractionRepository.getCurrentWalletAddress()
                },
                needReload = reload,
                errorMapper = { errorMessageFactory.create(it) },
                map = { state -> state }
            )

            _balanceStateFlow.applyFromExecuting(
                execute = {
                    walletInteractionRepository.getWalletBalance()
                },
                errorMapper = { errorMessageFactory.create(it) },
                map = { state -> state }
            )

        }
    }

    private fun String.isAmountValid(): Boolean {
        return decimalRegex.matches(this.trim())
    }

    fun enterSendAddress(address: String) {
        editorJob?.cancel()
        editorJob = viewModelScope.launch(appDispatcher.io) {
            _sendFormStateFlow.update {
                it.copy(sendAddress = EditTextState(address))
            }
        }
    }

    fun enterSendAmount(amount: String) {
        editorJob?.cancel()
        editorJob = viewModelScope.launch(appDispatcher.io) {
            _sendFormStateFlow.update {
                it.copy(sendAmount = EditTextState(amount))
            }
        }
    }

    fun send() {
        sendJob?.cancel()
        sendJob = viewModelScope.launch(appDispatcher.io) {
            _sendStateFlow.update { SendState.Default }

            val screenState = screenState.value
            if (screenState.sendingState == SendState.Sending) return@launch
            if (!screenState.sendFormState.canSend) return@launch

            val address = screenState.sendFormState.sendAddress.str.trim()
            val amount = screenState.sendFormState.sendAmount.str.trim()

            if (!isValidElectrumAddress(address)) return@launch
            if (!amount.isAmountValid()) return@launch
            val amountToSatoshi =
                amount.safeToBigDecimal()?.btcToMempoolSatoshi(RoundingMode.UP) ?: return@launch

            _sendStateFlow.update { SendState.Sending }
            walletInteractionRepository.sendCoins(
                destinationAddress = address,
                amount = amountToSatoshi,
            ).alsoIfSuccess { response ->
                _sendStateFlow.update {
                    SendState.SentSuccess(
                        response.txId.shortCryptoAddress(),
                        response.infoUrl
                    )
                }
            }.alsoIfError { response ->
                _sendStateFlow.update { SendState.SentError(errorMessageFactory.create(response.exception)) }
            }
        }
    }

    fun closeSendDialog() {
        viewModelScope.launch {
            _sendStateFlow.update { SendState.Default }
            updateWalletProfile(reload = false)
        }
    }

    companion object {
        private val decimalRegex = Regex("""^[+-]?(\d+([.]\d{0,12})?|[.]\d{1,12})${'$'}""")
    }
}
