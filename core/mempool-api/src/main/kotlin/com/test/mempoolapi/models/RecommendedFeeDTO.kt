package com.test.mempoolapi.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendedFeeDTO(
    @SerialName("fastestFee")
    val fastestFee : Long,
    @SerialName("halfHourFee")
    val halfHourFee : Long,
    @SerialName("hourFee")
    val hourFee : Long,
    @SerialName("economyFee")
    val economyFee : Long,
    @SerialName("minimumFee")
    val minimumFee : Long,
)