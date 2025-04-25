package com.test.wallet.data.repository.profile

import com.test.common.response.Response
import com.test.common.response.ResponseType
import com.test.commonextens.response.asNetworkError
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.mapIfSuccess
import com.test.commonextens.response.toResponse
import com.test.mempoolapi.MempoolApi
import com.test.wallet.data.mapper.toBalanceInfo
import com.test.wallet.data.source.profile.WalletProfileRemoteFileDataSource
import test.wallet.api.exception.WalletAddressBalanceNotMappedException
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletProfile
import test.wallet.api.repository.profile.WalletProfileRepository

class WalletProfileRepositoryImpl(
    private val mempoolApi: MempoolApi,
    private val walletProfileRemoteFileDataSource: WalletProfileRemoteFileDataSource
) : WalletProfileRepository {
    override suspend fun getProfile(): Response<WalletProfile> {
        return walletProfileRemoteFileDataSource.getWalletProfile()
    }

    override suspend fun getAddressBalance(address: String): Response<WalletAddressBalanceInfo> {
        return mempoolApi.getAddressInfo(address).toResponse(ResponseType.NETWORK)
            .mapIfSuccess {
                val balance = it.toBalanceInfo(address)
                balance?.asResponse() ?: WalletAddressBalanceNotMappedException().asNetworkError()
            }
    }
}