package com.test.transaction.data.mapper

import com.test.mempoolapi.models.InDTO
import com.test.mempoolapi.models.OutDTO
import com.test.mempoolapi.models.PrevOutputDTO
import com.test.mempoolapi.models.TransactionDTO
import com.test.mempoolapi.models.TransactionStatusDTO
import test.transaction.api.model.Input
import test.transaction.api.model.Output
import test.transaction.api.model.PrevOutput
import test.transaction.api.model.TransactionInfo
import test.transaction.api.model.TransactionStatus

internal fun TransactionDTO.toDomain(infoUrlProvider : () -> String?): TransactionInfo? {
    if (txId == null) return null
    return TransactionInfo(
        id = txId!!,
        vInput = vInDTO?.mapNotNull { it.toDomain() } ?: emptyList(),
        vOutput = vOutDTO?.mapNotNull { it.toDomain() } ?: emptyList(),
        fee = fee ?: 0L,
        transactionStatus = transactionStatusDto?.toTransactionStatus()
            ?: TransactionStatus.NOT_CONFIRMED,
        blockTime = transactionStatusDto?.blockTime,
        informationUrl = infoUrlProvider.invoke()
    )
}

internal fun InDTO.toDomain(): Input? {
    if (txId == null) return null
    return Input(
        txId = txId!!,
        vOut = vOut ?: 0,
        prevOutput = prevOutputDTO?.toDomain(),
    )
}

internal fun OutDTO.toDomain(): Output? {
    if (scriptPublicKey == null || scriptPublicKeyAddress == null) return null
    return Output(
        value = value ?: 0,
        scriptPublicKey = scriptPublicKey,
        scriptPublicKeyAddress = scriptPublicKeyAddress,
    )
}

internal fun PrevOutputDTO.toDomain(): PrevOutput? {
    if (scriptPublicKeyAddress == null) return null
    return PrevOutput(
        value = value ?: 0,
        scriptPublicKeyAddress = scriptPublicKeyAddress,
    )
}

internal fun TransactionStatusDTO.toTransactionStatus(): TransactionStatus {
    return if (confirmed == true) TransactionStatus.CONFIRMED
    else TransactionStatus.NOT_CONFIRMED
}