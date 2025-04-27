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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.test.bitcoinappuikit.composableDimens
import com.test.bitcoinappuikit.lce.LCEState
import com.test.bitcoinappuikit.lce.render
import com.test.bitcoinappuikit.string
import com.test.bitcoinappuikit.ui.CrSubText
import com.test.bitcoinappuikit.ui.CrSubTitleText
import com.test.bitcoinappuikit.ui.CrText
import com.test.bitcoinappuikit.ui.CrTitleText
import com.test.bitcoinappuikit.ui.InfoDialog
import com.test.bitcoinappuikit.ui.shimmerEffect
import com.test.feature.wallet.currentstate.R
import com.test.wallet.sendcoins.logic.SendCoinsViewModel
import com.test.wallet.sendcoins.logic.SendCoinsWalletState
import com.test.wallet.sendcoins.logic.SendFormState
import com.test.wallet.sendcoins.logic.SendState
import ru.alarmtrade.pandoracsinstaller.ui.view.buttons.CrButton

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
                title = { CrTitleText(text = string(R.string.send_coins_title)) },
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
                .padding(composableDimens().marginSmallSize.dp)
        ) {
            Column {
                Header(
                    wallet = { state.value.walletState },
                    onReload = { vm.updateWalletProfile() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(composableDimens().marginMediumSize.dp))

                SendForm(
                    sendFormState = { state.value.sendFormState },
                    onAddressChange = { vm.enterSendAddress(it) },
                    onAmountChange = { vm.enterSendAmount(it) },
                    onSend = { vm.send() },
                    modifier = Modifier.fillMaxWidth(),
                )

            }

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh state",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(composableDimens().iconMediumSize.dp)
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
            verticalArrangement = Arrangement.spacedBy(composableDimens().marginMediumSize.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.bitcoin_header),
                contentDescription = "",
                modifier = Modifier.size(composableDimens().iconXLargeSize.dp)
            )

            wallet.invoke().render(
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(composableDimens().marginMediumSize.dp),
                ) {
                    Button(onClick = {
                        showAddressDialog.value = info.currentAddress
                    }) {
                        CrText(string(R.string.header_address_title))
                    }
                    Row {
                        CrSubTitleText(
                            string(R.string.header_address_title) + ": ${info.addressBalanceIntBtc}",
                        )
                    }
                }

            }
        }
    }
    if (!showAddressDialog.value.isNullOrEmpty()) {
        InfoDialog(
            string(R.string.dialog_current_address_title),
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
    onSend: () -> Unit,
    modifier: Modifier
) {
    AnimatedVisibility(
        visible = sendFormState.invoke().isVisible,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(composableDimens().marginMediumSize.dp)
        ) {
            TextField(
                value = sendFormState.invoke().sendAddress.str,
                isError = !sendFormState.invoke().sendAddress.isValid,
                onValueChange = onAddressChange,
                label = { CrSubText(string(R.string.edit_address_to_send)) },
                modifier = modifier
            )

            TextField(
                value = sendFormState.invoke().sendAmount.str,
                isError = !sendFormState.invoke().sendAmount.isValid,
                onValueChange = onAmountChange,
                label = { CrSubText(string(R.string.edit_amount)) },
                modifier = modifier,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            CrButton(
                text = string(R.string.button_send_coins),
                onClick = { onSend.invoke() },
                enabled = sendFormState.invoke().canSend,
                modifier = Modifier.fillMaxWidth()
            )
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
                    CrTitleText(
                        text = when (dialogState.invoke()) {
                            SendState.Default -> string(R.string.sending_coins_state_default)
                            SendState.Sending -> string(R.string.sending_coins_state_sending)
                            is SendState.SentError -> string(R.string.sending_coins_state_sent_error)
                            is SendState.SentSuccess -> string(R.string.sending_coins_state_sent_success)
                        },
                    )
                    Spacer(modifier = Modifier.height(composableDimens().marginMediumSize.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val state = dialogState.invoke()) {
                            SendState.Sending -> {
                                CircularProgressIndicator(modifier = Modifier.size(composableDimens().iconLargeSize.dp))
                            }

                            SendState.Default -> {}
                            is SendState.SentError -> {
                                CrText(
                                    state.errorString ?: string(R.string.error_sent_coins_unknown_error),
                                )
                            }

                            is SendState.SentSuccess -> {
                                val textLayoutResult =
                                    remember { mutableStateOf<TextLayoutResult?>(null) }
                                val annotatedString = buildAnnotatedString {
                                    append(string(R.string.sending_coins_success_sub_body) + " ")

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

                                CrText(
                                    text = annotatedString,
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
                            CrText(string(R.string.button_send_reset_sent_dialog))
                        }
                    }

                    SendState.Sending -> {}
                }
            },
        )
    }
}
