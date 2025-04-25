package test.wallet.api.model

data class WalletProfile(
    val uuid: String,
    val availableAddress : List<WalletAddressInfo>
)

data class WalletAddressInfo(
    val address : String,
    val primaryKey : String,
)
