package com.test.bitcoinappuikit.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ru.alarmtrade.pandoracsinstaller.ui.view.buttons.CrButton

@Composable
fun InfoDialog(
    title: String,
    descr: String,
    onClose: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = onClose,
        title = { CrCustomText(
            text = title,
            textStyle = MaterialTheme.typography.titleSmall,
        ) },
        text = {
            CrText(descr)
        },
        confirmButton = {
            CrButton(
                onClick = {
                    onClose()
                },
                text = "OK"
            )
        },
    )
}