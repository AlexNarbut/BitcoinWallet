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
    cause: Throwable? = null,
    message: String? = null,
) : TransactionException(cause,message)

