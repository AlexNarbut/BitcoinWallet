package ru.alarmtrade.pandoracsinstaller.ui.view.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.test.bitcoinappuikit.composableDimens

val MIN_WIDTH = 24
val MIN_HEGHT = 24

@Composable
fun CrButton(
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    enabled: Boolean = true,
    outlined: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    contentPadding: PaddingValues = PaddingValues(
        start = composableDimens().buttonHorizontalPadding.dp,
        top = composableDimens().buttonVerticalPadding.dp,
        end = composableDimens().buttonHorizontalPadding.dp,
        bottom = composableDimens().buttonVerticalPadding.dp
    ),
) {
    CrButton(
        onClick,
        contentColor,
        borderColor,
        modifier
            .widthIn(MIN_WIDTH.dp)
            .heightIn(MIN_HEGHT.dp),
        text = {
            Text(
                text = text,
            )
        },
        enabled,
        outlined,
        interactionSource,
        contentPadding
    )
}

@Composable
fun CrButton(
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: @Composable () -> Unit,
    enabled: Boolean = true,
    outlined: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    contentPadding: PaddingValues = PaddingValues(
        start = composableDimens().buttonHorizontalPadding.dp,
        top = composableDimens().buttonVerticalPadding.dp,
        end = composableDimens().buttonHorizontalPadding.dp,
        bottom = composableDimens().buttonVerticalPadding.dp
    ),
) {
    if (outlined) {
        OutlinedButton(modifier = modifier
            .widthIn(MIN_WIDTH.dp)
            .heightIn(MIN_HEGHT.dp),
            onClick = {
                onClick()
            },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = borderColor, disabledContentColor = contentColor.copy(alpha = 0.5f)
            ),
            border = BorderStroke(composableDimens().cardBorderStrokeWidth.dp, borderColor),
            enabled = enabled,
            contentPadding = contentPadding,
            interactionSource = interactionSource ?: remember { MutableInteractionSource() }) {
            text()
        }
    } else {
        Button(modifier = modifier
            .widthIn(MIN_WIDTH.dp)
            .heightIn(MIN_HEGHT.dp),
            colors = ButtonColors(
                containerColor = borderColor,
                contentColor = contentColor,
                disabledContainerColor = borderColor.copy(alpha = 0.5f),
                disabledContentColor = contentColor.copy(alpha = 0.5f)
            ),
            enabled = enabled,
            onClick = {
                onClick()
            },
            contentPadding = contentPadding,
            interactionSource = interactionSource ?: remember { MutableInteractionSource() }) {
            text()
        }
    }
}

