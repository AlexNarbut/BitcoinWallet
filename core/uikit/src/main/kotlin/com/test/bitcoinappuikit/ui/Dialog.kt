package com.test.bitcoinappuikit.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun InfoDialog(
    title: String,
    descr: String,
    onClose: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(text = title, fontSize = 24.sp) },
        text = {
            Text(descr)
        },
        confirmButton = {
            Button(
                onClick = {
                    onClose()
                }
            ) {
                Text("ОК")
            }
        },
    )
}