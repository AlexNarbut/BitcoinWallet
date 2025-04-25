package com.test.mempoolapi.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UtxoDTO (
    @SerialName("txid")
    val txId: String, // transaction to spend from
    @SerialName("voutIndex")
    val vOutIndex: Long, // index of output in transaction
    @SerialName("value")
    val value: Long // UTXO amount (in satoshis)
)