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
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.content
import com.test.bitcoinappuikit.lce.render
import com.test.bitcoinappuikit.lce.renderContent
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
                title = { Text(text = "Bitcoin wallet") }//stringResource(R.string.app_name)) },
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
        ) {
            Header(
                headerState = { state.value.header },
                onReload = { vm.updateWalletProfile() },
                onSend = onSendCoins,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

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
    modifier: Modifier
) {
    val sendVisibilityState = rememberUpdatedState(headerState.invoke().content != null)
    val showAddressDialog = remember { mutableStateOf<String?>(null) }

    Box(modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.bitcoin_header),
                contentDescription = "",
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                headerState.invoke().render(
                    error = { error ->
                        Text(
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
                            Text(
                                "Address balance: ${info.addressBalanceIntBtc}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Text(
                                "Full balance: ${info.fullWalletBalanceIntBtc}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                }
            }
            AnimatedVisibility(
                visible = sendVisibilityState.value
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    IconButtonWithTitle(
                        title = "Send",
                        imageVector = Icons.Default.ArrowUpward,
                        iconColor = MaterialTheme.colorScheme.onPrimary,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        onClick = onSend,
                        iconModifier = Modifier.size(48.dp)
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
                        iconModifier = Modifier.size(48.dp)
                    )
                }
            }

        }
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh state",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .clickable { onReload() }
        )
    }
    if (!showAddressDialog.value.isNullOrEmpty()) {
        InfoDialog(
            "Current address",
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
            Text("Transactions of selected address")
            Spacer(Modifier.height(4.dp))
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
                        Text(text = "No transactions")
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
                                        .fillMaxWidth(),
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
            .padding(8.dp)
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
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = transaction.type.toTitle(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "ID ${transaction.transactionId}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if(transaction.timeString!= null){
                    Text(
                        text = "${transaction.timeString}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }
        }

        Text(
            text = "${transaction.amountIntBtc}",
            color = transaction.indicatorColor,
            style = MaterialTheme.typography.titleMedium
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
        Text(title)
    }
}

fun TransactionType.toTitle(): String = when (this) {
    TransactionType.UNKNOWN -> "Unknown"
    TransactionType.INCOME -> "Received"
    TransactionType.EXPENSE -> "Sent"
    TransactionType.SELF_TRANSFER -> "Self transfer"
}
