package test.transaction.api.model

data class TransactionHistoryInfo(
    val transactionInfo: TransactionInfo,
    val transactionType: TransactionType,
    val transactionAddress: TransactionAddress,
    val amountInSat: Long,
)

enum class TransactionType(val value: Int) {
    UNKNOWN(0), INCOME(1), EXPENSE(2), SELF_TRANSFER(3),
}

sealed class TransactionAddress {
    abstract val address: String?

    data class Send(override val address: String?) : TransactionAddress()

    data class Receive(override val address: String?) : TransactionAddress()

    data class Unknown(override val address: String? = null) : TransactionAddress()
}



