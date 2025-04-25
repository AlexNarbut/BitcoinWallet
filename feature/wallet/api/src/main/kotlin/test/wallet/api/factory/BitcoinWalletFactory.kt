package test.wallet.api.factory

import test.wallet.api.model.Wallet
import test.wallet.api.model.WalletProfile

interface BitcoinWalletFactory{
    fun create(deviceProfile: WalletProfile): Wallet
}
