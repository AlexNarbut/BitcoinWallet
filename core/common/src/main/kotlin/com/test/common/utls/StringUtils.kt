package com.test.common.utls

fun String.shortCryptoAddress(): String {
    if (length < 14) return this
    return this.take(7) + "..." + this.takeLast(7)
}