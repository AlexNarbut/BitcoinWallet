@file:OptIn(ExperimentalMaterial3Api::class)

package com.test.wallet.currentstate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.test.bitcoinappuikit.composableDimens
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.content
import com.test.bitcoinappuikit.lce.render
import com.test.bitcoinappuikit.lce.renderContent
import com.test.bitcoinappuikit.string
import com.test.bitcoinappuikit.ui.CrCustomText
import com.test.bitcoinappuikit.ui.CrSubText
import com.test.bitcoinappuikit.ui.CrSubTitleText
import com.test.bitcoinappuikit.ui.CrText
import com.test.bitcoinappuikit.ui.CrTitleText
import com.test.bitcoinappuikit.ui.DropdownSelector
import com.test.bitcoinappuikit.ui.InfoDialog
import com.test.bitcoinappuikit.ui.shimmerEffect
import com.test.feature.wallet.sendcoins.R
import com.test.wallet.currentstate.logic.CurrentStateHeader
import com.test.wallet.currentstate.logic.CurrentStateViewModel
import com.test.wallet.currentstate.logic.TransactionHistoryInfoViewModel
import test.transaction.api.model.TransactionType

@Preview(showBackground = true)
@Composable
private fun CurrentStateScreenPreview() {
    CurrentStateScreen(
        modifier = Modifier.fillMaxSize(),
        onSendCoins = {}
    )
}

@Composable
fun CurrentStateScreen(
    modifier: Modifier = Modifier,
    onSendCoins: () -> Unit
) {
    val viewModel = hiltViewModel<CurrentStateViewModel>()
    CurrentStateScreen(vm = viewModel, onSendCoins = onSendCoins, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CurrentStateScreen(
    vm: CurrentStateViewModel,
    onSendCoins: () -> Unit,
    modifier: Modifier
) {
    val state = vm.screenState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { CrTitleText(text = string(R.string.current_state_title)) },
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = composableDimens().marginSmallSize.dp),
        ) {
            Header(
                headerState = { state.value.header },
                onReload = { vm.updateWalletProfile() },
                onSend = onSendCoins,
                onAddressSelect = { vm.onChangeAddressState(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(composableDimens().marginLargeSize.dp))

            TransactionList(
                transactionListState = { state.value.transactions },
                isRefreshing = { state.value.isRefreshing },
                onRefresh = { vm.onPullToRefreshTrigger() },
                onErrorButtonClick = { vm.reloadTransactionHistory() },
                Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
internal fun Header(
    headerState: () -> LCEState<CurrentStateHeader>,
    onSend: () -> Unit,
    onReload: () -> Unit,
    onAddressSelect: (String) -> Unit,
    modifier: Modifier
) {
    val sendVisibilityState = rememberUpdatedState(headerState.invoke().content != null)
    val showAddressDialog = remember { mutableStateOf<String?>(null) }

    Box(modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(composableDimens().marginSmallSize.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.bitcoin_header),
                contentDescription = "",
                modifier = Modifier.size(composableDimens().iconXLargeSize.dp)
            )
            Spacer(Modifier.height(composableDimens().marginSmallSize.dp))

            headerState.invoke().render(
                error = { error ->
                    CrText(
                        error,
                        modifier.clickable {
                            onReload()
                        }
                    )
                },
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .shimmerEffect()
                    )
                },
            ) { info ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        CrSubTitleText(
                            "${string(R.string.current_state_header_address_balance)}: ${info.addressBalanceIntBtc}",
                        )
                    }
                    Spacer(Modifier.height(composableDimens().marginSmallSize.dp))
                    Row {
                        CrText(
                            "${string(R.string.current_state_header_full_balance)}: ${info.fullWalletBalanceIntBtc}",
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = sendVisibilityState.value
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(composableDimens().marginLargeSize.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(composableDimens().marginLargeSize.dp),
                    ) {
                        IconButtonWithTitle(
                            title = "Send",
                            imageVector = Icons.Default.ArrowUpward,
                            iconColor = MaterialTheme.colorScheme.onPrimary,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            onClick = onSend,
                            iconModifier = Modifier.size(composableDimens().iconLargeSize.dp)
                        )

                        IconButtonWithTitle(
                            title = "Address",
                            imageVector = Icons.Default.Info,
                            iconColor = MaterialTheme.colorScheme.onPrimary,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                showAddressDialog.value =
                                    headerState.invoke().content?.currentAddress ?: ""
                            },
                            iconModifier = Modifier.size(composableDimens().iconLargeSize.dp)
                        )
                    }

                    if ((headerState.invoke().content?.addressList?.addressList?.size ?: 0) > 1) {
                        DropdownSelector(
                            items = headerState.invoke().content?.addressList?.addressList
                                ?: emptyList(),
                            currentValue = headerState.invoke().content?.currentAddress ?: "",
                            title = string(R.string.current_state_header_available_addresses),
                            onValueChange = onAddressSelect,
                            modifier = Modifier
                                .padding(composableDimens().marginSmallSize.dp)
                                .fillMaxWidth()
                        )
                    }

                }
            }

        }
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh state",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(composableDimens().iconSmallSize.dp)
                .clickable { onReload() }
        )
    }
    if (!showAddressDialog.value.isNullOrEmpty()) {
        InfoDialog(
            string(R.string.current_state_current_address_dialog_title),
            showAddressDialog.value ?: "",
            onClose = { showAddressDialog.value = null }
        )
    }
}

@Composable
internal fun TransactionList(
    transactionListState: () -> LCEState<List<TransactionHistoryInfoViewModel>>,
    isRefreshing: () -> Boolean,
    onRefresh: () -> Unit,
    onErrorButtonClick: () -> Unit,
    modifier: Modifier
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing.invoke(),
        onRefresh = { onRefresh() },
        modifier = modifier
    ) {
        Column {
            CrText(string(R.string.current_state_selected_address_trans))

            Spacer(Modifier.height(composableDimens().marginXTinySize.dp))

            transactionListState.invoke().renderContent(
                modifier,
                onErrorButtonClick = {
                    onErrorButtonClick.invoke()
                }
            ) { content ->
                val uriHandler = LocalUriHandler.current
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    if (content.isEmpty()) {
                        CrText(text = string(R.string.current_state_no_trans))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            items(
                                content,
                                key = { it.transactionId }
                            ) { transaction ->
                                TransactionRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(composableDimens().marginTinySize.dp),
                                    transaction = transaction,
                                    onInfoClick = {
                                        uriHandler.openUri(it)
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun TransactionRow(
    modifier: Modifier,
    transaction: TransactionHistoryInfoViewModel,
    onInfoClick: (url: String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (!transaction.informationUrl.isNullOrEmpty())
                    onInfoClick(transaction.informationUrl)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (transaction.type == TransactionType.INCOME)
                    Icons.Outlined.ArrowCircleDown
                else Icons.Outlined.ArrowCircleUp,
                contentDescription = "Transaction Icon",
                tint = transaction.indicatorColor,
                modifier = Modifier.size(composableDimens().iconMediumSize.dp)
            )

            Spacer(modifier = Modifier.width(composableDimens().marginSmallSize.dp))

            Column {
                CrText(
                    text = transaction.type.toTitle(),
                )
                CrSubText(
                    text = "ID ${transaction.transactionId}",
                )

                if (transaction.timeString != null) {
                    Spacer(modifier = Modifier.height(composableDimens().marginTinySize.dp))
                    CrCustomText(
                        text = transaction.timeString,
                        textStyle = MaterialTheme.typography.labelSmall,
                    )
                }

            }
        }

        CrText(
            text = transaction.amountIntBtc,
            color = transaction.indicatorColor,
        )
    }
}

@Composable
fun IconButtonWithTitle(
    title: String,
    imageVector: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    iconModifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = iconModifier,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = backgroundColor,
                contentColor = iconColor,
            )
        ) {
            Icon(imageVector, contentDescription = null, tint = iconColor)
        }
        CrText(title)
    }
}

@Composable
fun TransactionType.toTitle(): String = when (this) {
    TransactionType.UNKNOWN -> string(R.string.transaction_unknown_state)
    TransactionType.INCOME -> string(R.string.transaction_income_state)
    TransactionType.EXPENSE -> string(R.string.transaction_expense_state)
    TransactionType.SELF_TRANSFER -> string(R.string.transaction_self_transfer_state)
}
