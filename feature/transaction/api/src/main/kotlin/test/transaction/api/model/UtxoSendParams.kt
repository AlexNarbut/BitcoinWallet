package test.transaction.api.model

data class UtxoSendParams(
    val txId: String,
    val vOutIndex: Long,
    val valueInSat: Long
)