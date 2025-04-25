package com.test.transaction.data.utils

import java.lang.NumberFormatException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private const val M_BTC_MULTIPLIER = 100_000_000.0
private val decimalFormat = DecimalFormat("#,##0.########")

fun Long.mempoolSatoshiTotBtc(): BigDecimal {
    return BigDecimal(this)
        .divide(M_BTC_MULTIPLIER.toBigDecimal(), 8, RoundingMode.DOWN)
}

fun String.safeToBigDecimal(): BigDecimal? {
    return try {
        BigDecimal(this)
    } catch (ex: NumberFormatException) {
        null
    }
}

fun Double.safeToBigDecimal(): BigDecimal? {
    return try {
        BigDecimal(this)
    } catch (ex: NumberFormatException) {
        null
    }
}

fun BigDecimal.btcToMempoolSatoshi(roundingMode: RoundingMode): Long {
    return this
        .multiply(M_BTC_MULTIPLIER.toBigDecimal())
        .setScale(0, roundingMode)
        .longValueExact()
}

fun BigDecimal.tBtcString(): String {
    return decimalFormat.format(this)
}