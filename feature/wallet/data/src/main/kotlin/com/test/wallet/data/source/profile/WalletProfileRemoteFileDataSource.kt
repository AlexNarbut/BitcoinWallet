package com.test.wallet.data.source.profile

import com.test.common.response.Response
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.getValueIfSuccess
import test.wallet.api.model.WalletProfile
import test.wallet.api.provider.WalletAddressInfosProvider

class WalletProfileRemoteFileDataSource(
    private val defaultWalletUUID: String,
    private val walletAddressInfosProvider: WalletAddressInfosProvider
) {

    fun getWalletProfile(): Response<WalletProfile> {
        return loadProfileFromFile()
    }

    private fun loadProfileFromFile(): Response<WalletProfile> {
        val addressInfoResponse = walletAddressInfosProvider.get()
        if (addressInfoResponse is Response.Error) return addressInfoResponse

        return WalletProfile(
            uuid = defaultWalletUUID,
            availableAddress = addressInfoResponse.getValueIfSuccess() ?: emptyList()
        ).asResponse()
    }

}