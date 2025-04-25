package com.test.transaction.data.utils

import org.bitcoinj.base.AddressParser

fun isValidElectrumAddress(address: String): Boolean {
    return try {
        AddressParser.getDefault().parseAddress(address)
        true
    } catch (e: Exception) {
        false
    }
}