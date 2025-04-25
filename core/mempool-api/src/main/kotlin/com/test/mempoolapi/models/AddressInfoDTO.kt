package com.test.mempoolapi.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressInfoDTO (
    @SerialName("chain_stats")
    val chainStats: ChainStats? = null
)

@Serializable
data class ChainStats (
    @SerialName("funded_txo_sum")
    val fundedSum: Long? = null,
    @SerialName("spent_txo_sum")
    val spentSum: Long? = null
)