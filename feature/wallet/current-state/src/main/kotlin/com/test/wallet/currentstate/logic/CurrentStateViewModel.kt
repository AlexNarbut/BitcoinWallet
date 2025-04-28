package com.test.wallet.currentstate.logic

import androidx.lifecycle.viewModelScope
import com.test.bitcoinappuikit.vm.BaseViewModel
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.applyFromExecuting
import com.test.bitcoinappuikit.lce.asLCEState
import com.test.bitcoinappuikit.lce.ifSuccess
import com.test.bitcoinappuikit.lce.isLoading
import com.test.bitcoinappuikit.lce.mapContent
import com.test.common.AppDispatchers
import com.test.common.exception.ErrorMessageFactory
import com.test.wallet.currentstate.mapper.getHeaderState
import com.test.wallet.currentstate.mapper.toViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import test.transaction.api.model.TransactionHistoryInfo
import test.wallet.api.model.WalletBalanceInfo
import test.wallet.api.repository.interaction.WalletInteractionRepository
import javax.inject.Inject

@HiltViewModel
class CurrentStateViewModel @Inject constructor(
    private val appDispatcher: AppDispatchers,
    private val walletInteractionRepository: WalletInteractionRepository,
    private val errorMessageFactory: ErrorMessageFactory
) : BaseViewModel() {

    private val _addressStateFlow = MutableStateFlow<LCEState<String>>(LCEState.None)
    private val _addressListStateFlow = MutableStateFlow<LCEState<List<String>>>(LCEState.None)
    private val _balanceStateFlow = MutableStateFlow<LCEState<WalletBalanceInfo>>(LCEState.None)
    private val _transactionHistoryFlow =
        MutableStateFlow<LCEState<List<TransactionHistoryInfo>>>(LCEState.None)
    private val _isTransactionRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<CryptoScreenState> = combine(
        _addressStateFlow,
        _addressListStateFlow,
        _balanceStateFlow,
        _transactionHistoryFlow,
        _isTransactionRefreshing
    ) { addressState, addressListState, balance, transactionHistory, isRefreshing ->
        val headerState = getHeaderState(addressState, addressListState, balance)

        val transactionUIState = transactionHistory.mapContent { it.map { it.toViewModel() } }

        val updatedRefreshingState = isRefreshing && transactionUIState.isLoading()

        CryptoScreenState(
            header = headerState,
            transactions = transactionUIState,
            isRefreshing = updatedRefreshingState
        )
    }.flowOn(appDispatcher.default)
        .onStart {
            updateWalletAddressState()
            listenChangeAddressState()
            loadAddressListState()
        }
        .stateIn(viewModelScope, started = SharingStarted.Lazily, CryptoScreenState())

    private var updateWalletAddressStateJob: Job? = null
    private var updateWalletAddressListenJob: Job? = null
    private var listenChangeWalletAddressStateJob: Job? = null
    private var updateBalanceJob: Job? = null
    private var updateTransactionJob: Job? = null
    private var changeAddressJob: Job? = null


    private fun updateWalletAddressState() {
        updateWalletAddressStateJob?.cancel()
        updateWalletAddressStateJob = viewModelScope.launch(appDispatcher.io) {
            walletInteractionRepository.getCurrentWalletAddressFlow().collectLatest { state ->
                _addressStateFlow.update { state.asLCEState({ errorMessageFactory.create(it) }) }
            }
        }
    }

    private fun listenChangeAddressState() {
        listenChangeWalletAddressStateJob?.cancel()
        listenChangeWalletAddressStateJob = viewModelScope.launch(appDispatcher.io) {
            _addressStateFlow.collectLatest { state ->
                _addressStateFlow.value.ifSuccess {
                    updateWalletProfile()
                }
            }
        }
    }

    fun onChangeAddressState(address : String) {
        changeAddressJob?.cancel()
        changeAddressJob = viewModelScope.launch(appDispatcher.io) {
           walletInteractionRepository.setCurrentWalletAddress(address)
        }
    }

    private fun loadAddressListState() {
        updateWalletAddressListenJob?.cancel()
        updateWalletAddressListenJob = viewModelScope.launch(appDispatcher.io) {
            _addressListStateFlow.applyFromExecuting(
                execute = {
                    walletInteractionRepository.getAvailableWalletAddresses()
                },
                errorMapper = { errorMessageFactory.create(it) },
                map = { state -> state }
            )
        }
    }

    fun updateWalletProfile() {
        updateWalletBalance()
        reloadTransactionHistory()
    }

    private fun updateWalletBalance() {
        updateBalanceJob?.cancel()
        updateBalanceJob = safeLaunch(appDispatcher.io) {
            _balanceStateFlow.applyFromExecuting(
                execute = {
                    walletInteractionRepository.getWalletBalance()
                },
                errorMapper = { errorMessageFactory.create(it) },
                map = { state -> state }
            )
        }
    }

    fun reloadTransactionHistory() {
        updateTransactionJob?.cancel()
        updateTransactionJob = safeLaunch(appDispatcher.io) {
            _transactionHistoryFlow.applyFromExecuting(
                execute = {
                    walletInteractionRepository.getTransactionHistory()
                },
                errorMapper = { errorMessageFactory.create(it) },
                map = { state -> state }
            )
        }
    }

    fun onPullToRefreshTrigger() {
        _isTransactionRefreshing.update { true }
        reloadTransactionHistory()
    }
}
