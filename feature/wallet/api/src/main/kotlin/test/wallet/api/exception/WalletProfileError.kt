package test.wallet.api.exception

sealed class  WalletProfileException(
    cause: Throwable? = null,
    message: String? = null,
) : Exception(message, cause)

class WalletProfileNotInitializedException(
    cause: Throwable? = null,
    message: String? = null,
) : WalletProfileException(cause,message)

class WalletProfileNotLoadFromFileException(
    cause: Throwable? = null,
    message: String? = null,
) : WalletProfileException(cause,message)

class WalletAddressBalanceNotMappedException(
    cause: Throwable? = null,
    message: String? = null,
) : WalletProfileException(cause,message)

class WalletAddressBalanceNotFoundException(
    cause: Throwable? = null,
    message: String? = null,
) : WalletProfileException(cause,message)