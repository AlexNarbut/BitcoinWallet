package test.wallet.api.repository.profile

import com.test.common.response.Response
import test.wallet.api.model.WalletAddressBalanceInfo
import test.wallet.api.model.WalletProfile

interface WalletProfileRepository {
    suspend fun getProfile() : Response<WalletProfile>
    suspend fun getAddressBalance(address : String) : Response<WalletAddressBalanceInfo>
}