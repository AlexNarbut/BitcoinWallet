package com.test.wallet.currentstate.logic

import androidx.lifecycle.viewModelScope
import com.test.bitcoinappuikit.base.BaseViewModel
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.applyFromExecuting
import com.test.bitcoinappuikit.lce.ifSuccess
import com.test.bitcoinappuikit.lce.isLoading
import com.test.bitcoinappuikit.lce.mapContent
import com.test.common.AppDispatchers
import com.test.common.exception.ErrorMessageFactory
import com.test.wallet.currentstate.mapper.getHeaderState
import com.test.wallet.currentstate.mapper.toViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val _balanceStateFlow = MutableStateFlow<LCEState<WalletBalanceInfo>>(LCEState.None)
    private val _transactionHistoryFlow =
        MutableStateFlow<LCEState<List<TransactionHistoryInfo>>>(LCEState.None)
    private val _isTransactionRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<CryptoScreenState> = combine(
        _addressStateFlow,
        _balanceStateFlow,
        _transactionHistoryFlow,
        _isTransactionRefreshing
    ) { addressState, balance, transactionHistory, isRefreshing ->
        val transactionUIState = transactionHistory.mapContent { it.map { it.toViewModel() } }
        val headerState = getHeaderState(addressState, balance)

        val updatedRefreshingState = isRefreshing && transactionUIState.isLoading()

        CryptoScreenState(
            header = headerState,
            transactions = transactionUIState,
            isRefreshing = updatedRefreshingState
        )
    }.flowOn(appDispatcher.default)
        .stateIn(viewModelScope, started = SharingStarted.Lazily, CryptoScreenState())

    private var updateWalletProfileJob: Job? = null
    private var updateBalanceJob: Job? = null
    private var updateTransactionJob: Job? = null


    init {
        viewModelScope.launch {
            prepareWallet()
        }
    }

    private suspend fun prepareWallet() = withContext(appDispatcher.default) {
        walletInteractionRepository.init(needRelease = true)
        updateWalletProfile()
    }

    fun updateWalletProfile() {
        updateWalletProfileJob?.cancel()
        updateWalletProfileJob = safeLaunch(appDispatcher.io) {
            _addressStateFlow.applyFromExecuting(
                execute = {
                    walletInteractionRepository.getCurrentWalletAddress()
                },
                errorMapper = { errorMessageFactory.create(it) },
                map = { state -> state }
            )
            _addressStateFlow.value.ifSuccess {
                updateWalletBalance()
                reloadTransactionHistory()
            }
        }
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
