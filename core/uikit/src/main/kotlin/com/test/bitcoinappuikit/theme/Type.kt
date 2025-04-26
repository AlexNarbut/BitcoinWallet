package com.test.bitcoinappuikit.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
fun getTypography(dimensImpl: DimensImpl) =
    Typography(
        displayLarge = TextStyle(
            fontSize = dimensImpl.textH1Size.sp,
            fontWeight = FontWeight.Normal,
        ), displayMedium = TextStyle(
            fontSize = dimensImpl.textH2Size.sp,
            fontWeight = FontWeight.Bold,
        ), displaySmall = TextStyle(
            fontSize = dimensImpl.textH3Size.sp,
            fontWeight = FontWeight.SemiBold,
        ), headlineLarge = TextStyle(
            fontSize = dimensImpl.textH5Size.sp,
            fontWeight = FontWeight.SemiBold,
        ), headlineMedium = TextStyle(
            fontSize = dimensImpl.textH5Size.sp,
            fontWeight = FontWeight.SemiBold,
        ), headlineSmall = TextStyle(
            fontSize = dimensImpl.textH5Size.sp,
            fontWeight = FontWeight.SemiBold,
        ), titleLarge = TextStyle(
            fontSize = dimensImpl.textH6Size.sp,
            fontWeight = FontWeight.Bold,
        ), titleMedium = TextStyle(
            fontSize = dimensImpl.textSubTitle1Size.sp,
            fontWeight = FontWeight.SemiBold,
        ), titleSmall = TextStyle(
            fontSize = dimensImpl.textSubTitle2Size.sp,
            fontWeight = FontWeight.SemiBold,
        ), bodyLarge = TextStyle(
            fontSize = dimensImpl.textBody1Size.sp,
            fontWeight = FontWeight.Normal,

            ), bodyMedium = TextStyle(
            fontSize = dimensImpl.textBody1Size.sp,
            fontWeight = FontWeight.SemiBold,
        ), bodySmall = TextStyle(
            fontSize = dimensImpl.textCaptionSize.sp,
            fontWeight = FontWeight.Normal,
        ), labelLarge = TextStyle(
            fontSize = dimensImpl.textButtonSize.sp,
            fontWeight = FontWeight.Normal,
        ), labelMedium = TextStyle(
            fontSize = dimensImpl.textOverlineSize.sp,
            fontWeight = FontWeight.Normal,
        ), labelSmall = TextStyle(
            fontSize = dimensImpl.textOverlineSize.sp,
            fontWeight = FontWeight.Normal,
        )
    )
