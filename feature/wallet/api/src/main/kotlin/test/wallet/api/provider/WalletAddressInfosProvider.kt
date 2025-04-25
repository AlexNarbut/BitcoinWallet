package test.wallet.api.provider

import com.test.common.response.Response
import test.wallet.api.model.WalletAddressInfo

interface WalletAddressInfosProvider {
    fun get(): Response<List<WalletAddressInfo>>
}