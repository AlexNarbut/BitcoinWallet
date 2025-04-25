package test.wallet.api.model

data class WalletBalanceInfo(
    val addressBalances : List<WalletAddressBalanceInfo>,
    val fullAmountInSat : Long
){
    companion object {
        val Default = WalletBalanceInfo(
            addressBalances = emptyList(),
            fullAmountInSat = 0
        )
    }
}

data class WalletAddressBalanceInfo(
    val address : String,
    val amountInSat : Long
){
    companion object {
        fun getDefault(address : String) = WalletAddressBalanceInfo(
            address = address,
            amountInSat = 0
        )
    }
}

enum class Currency{
    SATOSHI
}