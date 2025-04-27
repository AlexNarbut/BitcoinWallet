package test.transaction.api.exception

sealed class  TransactionException(
    cause: Throwable? = null,
    message: String? = null,
) : Exception(message, cause)

class TransactionUtxoNotFoundException(
    cause: Throwable? = null,
    message: String? = null,
) : TransactionException(cause,message)

class TransactionNotEnoughCoinsException(
    val inputCoinsInSat : Long,
    val neededCoinsWithFeeInSat : Long,
    cause: Throwable? = null,
    message: String? = null,
) : TransactionException(cause,message)

class TransactionSendErrorException(
    cause: Throwable? = null,
    message: String? = null,
) : TransactionException(cause,message)

