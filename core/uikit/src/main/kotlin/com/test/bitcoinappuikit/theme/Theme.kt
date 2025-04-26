package com.test.bitcoinappuikit.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val lightColors = lightColorScheme(
    primary = Color(0xFF183C7A),
    onPrimary = Color(0xFFf9f9f9),

    secondary = Color(0xFF0371C5),
    onSecondary = Color(0xFFf9f9f9),


    surface = Color(0xFFECECEC),
    onSurface = TextBlack,

    background = Color(0xFFf9f9f9),
    onBackground = TextBlack,

    error = Color(0xFFDD1B5D),
    onError = Color(0xFFf9f9f9),
)

val darkColors = darkColorScheme(
    primary = Color(0xFF3395FF),
    onPrimary = Color(0xFFE6E7F1),


    secondary = Color(0xFF70B0F4),
    onSecondary = Color(0xFFE6E7F1),


    surface = Color(0xFF2C2C40),
    onSurface = Color(0xFFE6E7F1),

    background = Color(0xFF252529),
    onBackground = Color(0xFFE6E7F1),

    error = Color(0xFFDD1B5D),
    onError = Color(0xFFE6E7F1),
)


@Composable
fun CryptoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    dimensImpl: DimensImpl = DEFAULT_APP_DIMENS.getDimensClass(),
    content: @Composable () -> Unit
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> darkColors
            else -> lightColors
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(dimensImpl),
        content = content
    )
}
