@file:OptIn(ExperimentalMaterial3Api::class)

package com.test.wallet.sendcoins.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.render
import com.test.bitcoinappuikit.ui.InfoDialog
import com.test.bitcoinappuikit.ui.shimmerEffect
import com.test.feature.wallet.currentstate.R
import com.test.wallet.sendcoins.logic.SendCoinsViewModel
import com.test.wallet.sendcoins.logic.SendCoinsWalletState
import com.test.wallet.sendcoins.logic.SendFormState
import com.test.wallet.sendcoins.logic.SendState

@Preview(showBackground = true)
@Composable
private fun SendCoinsScreenPreview() {
    SendCoinsScreen(
        modifier = Modifier.fillMaxSize(),
        onBack = {}
    )
}

@Composable
fun SendCoinsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<SendCoinsViewModel>()
    SendCoinsScreen(vm = viewModel, onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SendCoinsScreen(
    vm: SendCoinsViewModel,
    onBack: () -> Unit,
    modifier: Modifier
) {
    val state = vm.screenState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Send coins") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            Column {
                Header(
                    wallet = { state.value.walletState },
                    onReload = { vm.updateWalletProfile() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                SendForm(
                    sendFormState = { state.value.sendFormState },
                    onAddressChange = { vm.enterSendAddress(it) },
                    onAmountChange = { vm.enterSendAmount(it) },
                    onFeeAmountChange = { vm.enterFeeAmount(it) },
                    onSend = { vm.send() },
                    modifier = Modifier.fillMaxWidth(),
                )

            }

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh state",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clickable { vm.updateWalletProfile() }
            )

            SendDialog(
                dialogState = { state.value.sendingState },
                onDismissRequest = { vm.closeSendDialog() }
            )
        }

    }
}

@Composable
internal fun Header(
    wallet: () -> LCEState<SendCoinsWalletState>,
    onReload: () -> Unit,
    modifier: Modifier
) {
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
                wallet.invoke().render(
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(onClick = {
                            showAddressDialog.value = info.currentAddress
                        }) {
                            Text("Address")
                        }
                        Row {
                            Text(
                                "Address balance: ${info.addressBalanceIntBtc}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                }
            }
        }
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
internal fun SendForm(
    sendFormState: () -> SendFormState,
    onAddressChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onFeeAmountChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier
) {
    AnimatedVisibility(
        visible = sendFormState.invoke().isVisible,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = sendFormState.invoke().sendAddress.str,
                isError = !sendFormState.invoke().sendAddress.isValid,
                onValueChange = onAddressChange,
                label = { Text("Address to Send") },
                modifier = modifier
            )

            TextField(
                value = sendFormState.invoke().sendAmount.str,
                isError = !sendFormState.invoke().sendAmount.isValid,
                onValueChange = onAmountChange,
                label = { Text("Amount") },
                modifier = modifier,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = sendFormState.invoke().feeAmount.str,
                isError = !sendFormState.invoke().feeAmount.isValid,
                onValueChange = onFeeAmountChange,
                label = { Text("Fee amount") },
                modifier = modifier,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Button(
                onClick = { onSend.invoke() },
                enabled = sendFormState.invoke().canSend,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun SendDialog(
    dialogState: () -> SendState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogVisible = rememberUpdatedState(dialogState.invoke() != SendState.Default)
    if (dialogVisible.value) {
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = when (dialogState.invoke()) {
                            SendState.Default -> "Preparing"
                            SendState.Sending -> "Sending"
                            is SendState.SentError -> "Error"
                            is SendState.SentSuccess -> "Coins were sent success!"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val state = dialogState.invoke()) {
                            SendState.Sending -> {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            }

                            SendState.Default -> {}
                            is SendState.SentError -> {
                                Text(
                                    state.errorString ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }

                            is SendState.SentSuccess -> {
                                val textLayoutResult =
                                    remember { mutableStateOf<TextLayoutResult?>(null) }
                                val annotatedString = buildAnnotatedString {
                                    append("Your transaction ID is ")

                                    val startIndex = length
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    ) {
                                        append(state.shortTransactionId)
                                    }

                                    val endIndex = length
                                    addStringAnnotation(
                                        tag = "transaction_id",
                                        annotation = state.shortTransactionId,
                                        start = startIndex,
                                        end = endIndex
                                    )
                                    append(".")
                                }

                                Text(
                                    text = annotatedString,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures { offsetPosition ->
                                            textLayoutResult.value?.let { layoutResult ->
                                                val position =
                                                    layoutResult.getOffsetForPosition(offsetPosition)
                                                annotatedString.getStringAnnotations(
                                                    "transaction_id",
                                                    position,
                                                    position
                                                )
                                                    .firstOrNull()?.let { _ ->
                                                        uriHandler.openUri(state.informationUrl)
                                                    }
                                            }
                                        }
                                    },
                                    onTextLayout = { textLayoutResult.value = it }
                                )
                            }
                        }
                    }
                }

            },
            confirmButton = {
                when (dialogState.invoke()) {
                    is SendState.SentError,
                    is SendState.SentSuccess,
                    SendState.Default -> {
                        TextButton(onClick = {
                            onDismissRequest.invoke()
                        }) {
                            Text("Ok")
                        }
                    }

                    SendState.Sending -> {}
                }
            },
        )
    }
}
