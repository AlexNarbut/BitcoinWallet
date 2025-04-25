package com.test.bitcoinappuikit.lce

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun LoadingError(errorText: String, modifier: Modifier, onErrorButtonClick: (() -> Unit)?) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = errorText,
                textAlign = TextAlign.Center
            )
            onErrorButtonClick?.let {
                Button(
                    onClick = it
                ) {
                    Text("Fix")
                }
            }
        }
    }
}

@Composable
fun Loading(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun <T> LCEState<T>.render(
    error: @Composable ((error: String) -> Unit),
    loading: @Composable () -> Unit = { Loading() },
    content: @Composable (content: T) -> Unit
) {
    when (this) {
        is LCEState.Content -> content(this.content)
        is LCEState.Loading -> loading()
        is LCEState.Error -> error(this.error)
        else -> Unit
    }
}

@Composable
fun <T> LCEState<T>.render(
    modifier: Modifier,
    onErrorButtonClick: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { Loading(modifier) },
    render: @Composable (content: T) -> Unit
) {
    when (this) {
        is LCEState.Content -> render(this.content)
        is LCEState.Loading -> loading()
        is LCEState.Error -> LoadingError(this.error, modifier, onErrorButtonClick)
        is LCEState.None -> {
        }
    }
}

@Composable
fun <T> LCEState<T>.renderContent(
    modifier: Modifier,
    onErrorButtonClick: (() -> Unit)? = null,
    render: @Composable (content: T) -> Unit
) {
    Box(modifier = modifier) {
        render(modifier, onErrorButtonClick, render = render)
    }
}