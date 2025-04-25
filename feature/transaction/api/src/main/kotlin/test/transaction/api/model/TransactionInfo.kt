package test.transaction.api.model

import java.util.Date


data class TransactionInfo(
    val id: String,
    val transactionStatus: TransactionStatus,
    val vInput: List<Input>,
    val vOutput: List<Output>,
    val fee: Long,
    val blockTime: Date?,
    val informationUrl : String?
)

data class Input(
    val txId: String,
    val vOut: Int,
    val prevOutput: PrevOutput?
)

data class Output(
    val value: Long,
    val scriptPublicKey: String? = null,
    val scriptPublicKeyAddress: String? = null
)

enum class TransactionStatus {
    NOT_CONFIRMED, CONFIRMED;
}

data class PrevOutput(
    val value: Long,
    val scriptPublicKeyAddress: String? = null
)