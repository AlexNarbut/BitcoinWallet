package com.test.wallet.data.mapper

import test.transaction.api.model.TransactionAddress
import test.transaction.api.model.TransactionHistoryInfo
import test.transaction.api.model.TransactionInfo
import test.transaction.api.model.TransactionType

internal fun TransactionInfo.toHistory(walletAddresses: List<String>): TransactionHistoryInfo? {
    // выясняем, есть ли наш адрес в списке in.
    // Если да, то это операция расхода.
    val isOutgoing = vInput.any { input ->
        input.prevOutput?.scriptPublicKeyAddress in walletAddresses
    }

    // есть ли кто-то ещё кроме нас в списке out
    val hasOutputToOthers = vOutput.any { out ->
        out.scriptPublicKeyAddress !in walletAddresses
    }

    // определяем, что это поступление средств к нам
    val isIncoming = !isOutgoing && vOutput.any { out ->
        out.scriptPublicKeyAddress in walletAddresses
    }

    // определяем тип транзакции
    val transactionType: TransactionType = when {
        isOutgoing && hasOutputToOthers -> TransactionType.EXPENSE
        isIncoming -> TransactionType.INCOME
        isOutgoing && !hasOutputToOthers -> TransactionType.SELF_TRANSFER
        else -> TransactionType.UNKNOWN
    }

    val amount: Long = when (transactionType) {
        // если тратим, то нужно прибавить к отображаемой сумме величину комиссии
        TransactionType.EXPENSE -> vOutput
            .filter { it.scriptPublicKeyAddress !in walletAddresses }
            .sumOf { it.value } + fee

        // если получаем, просто выводим сумму трансфера
        TransactionType.INCOME -> vOutput
            .filter { it.scriptPublicKeyAddress in walletAddresses }
            .sumOf { it.value }

        TransactionType.SELF_TRANSFER -> vOutput
            .filter { it.scriptPublicKeyAddress in walletAddresses }
            .sumOf { it.value } + fee

        else -> 0L
    }

    val transactionAddress: TransactionAddress = when (transactionType) {
        // для поступлений ищем кошелёк, с которого переведены деньги
        TransactionType.INCOME -> {
            val senderAddress = vInput.firstOrNull { input ->
                input.prevOutput?.scriptPublicKeyAddress !in walletAddresses
            }?.prevOutput?.scriptPublicKeyAddress ?: ""

            TransactionAddress.Send(senderAddress)
        }

        // для расхода - кошелёк, на который они переведены
        TransactionType.EXPENSE -> {
            val receiverAddress = vOutput.firstOrNull { out ->
                out.scriptPublicKeyAddress !in walletAddresses
            }?.scriptPublicKeyAddress

            TransactionAddress.Receive(receiverAddress)
        }

        else -> TransactionAddress.Unknown()
    }

    return TransactionHistoryInfo(
        transactionInfo = this,
        transactionType = transactionType,
        transactionAddress = transactionAddress,
        amountInSat = amount
    )
}