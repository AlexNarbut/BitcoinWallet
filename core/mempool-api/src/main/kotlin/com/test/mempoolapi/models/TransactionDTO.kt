package com.test.mempoolapi.models

import com.test.mempoolapi.utils.DateUnixTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class TransactionDTO(
    @SerialName("txid")
    val txId: String? = null,
    @SerialName("vin")
    val vInDTO: List<InDTO>? = null,
    @SerialName("vout")
    val vOutDTO: List<OutDTO>? = null,
    @SerialName("fee")
    val fee: Long? = null,
    @SerialName("status")
    val transactionStatusDto: TransactionStatusDTO? = null,
)

@Serializable
data class InDTO(
    @SerialName("txid")
    val txId: String? = null,
    @SerialName("vout")
    val vOut: Int? = null,
    @SerialName("prevout")
    val prevOutputDTO: PrevOutputDTO? = null,
)

@Serializable
data class OutDTO(
    @SerialName("value")
    val value: Long? = null,
    @SerialName("scriptpubkey")
    val scriptPublicKey: String? = null,
    @SerialName("scriptpubkey_address")
    val scriptPublicKeyAddress: String? = null,
)

@Serializable
data class TransactionStatusDTO(
    @SerialName("confirmed")
    val confirmed: Boolean? = null,
    @SerialName("block_time")
    @Serializable(with = DateUnixTimestampSerializer::class)
    val blockTime: Date? = null,
)

@Serializable
data class PrevOutputDTO(
    @SerialName("value")
    val value: Long? = null,
    @SerialName("scriptpubkey_address")
    val scriptPublicKeyAddress: String? = null
)