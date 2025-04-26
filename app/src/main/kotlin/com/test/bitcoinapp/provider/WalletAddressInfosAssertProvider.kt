package com.test.bitcoinapp.provider

import android.content.Context
import com.test.common.response.Response
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.getExceptionIfError
import com.test.commonextens.response.mapValueIfSuccess
import com.test.commonextens.utils.readDataFromAssertFile
import test.wallet.api.exception.WalletProfileNotLoadFromFileException
import test.wallet.api.model.WalletAddressInfo
import test.wallet.api.provider.WalletAddressInfosProvider

class WalletAddressInfosAssertProvider(
    private val context: Context,
    private val addressFilePath: String,
    private val keysFilePath: String,
) : WalletAddressInfosProvider {


    override fun get(): Response<List<WalletAddressInfo>> {
        val addressesDataResponse = loadAddressesFromFile()
        val keysDataResponse = loadPrivateKeyFromFile()
        if (addressesDataResponse !is Response.Success || addressesDataResponse.value.isEmpty()) {
            return Response.Error.General(
                WalletProfileNotLoadFromFileException(
                    addressesDataResponse.getExceptionIfError(),
                    "Addresses Data are not loaded from file (path $addressFilePath)"
                )
            )
        }
        if (keysDataResponse !is Response.Success || keysDataResponse.value.isEmpty()) {
            return Response.Error.General(
                WalletProfileNotLoadFromFileException(
                    keysDataResponse.getExceptionIfError(),
                    "Keys Data are not loaded from file (path $keysFilePath)"
                )
            )
        }

        return getWalletAddressInfoFromFileData(
            addressesDataResponse.value,
            keysDataResponse.value
        ).asResponse()
    }

    private fun loadAddressesFromFile(): Response<List<String>> {
        val addressesString = readDataFromAssertFile(context, addressFilePath)
        return addressesString.mapValueIfSuccess {
            it.split("\r\n", "\n").filter { it.isNotEmpty() }
        }
    }

    private fun loadPrivateKeyFromFile(): Response<List<String>> {
        val fileString = readDataFromAssertFile(context, keysFilePath)
        return fileString.mapValueIfSuccess {
            it.split("\r\n", "\n").filter { it.isNotEmpty() }
        }
    }

    private fun getWalletAddressInfoFromFileData(
        addresses: List<String>,
        keys: List<String>
    ): List<WalletAddressInfo> {
        val loopSize = addresses.size
        val list = mutableListOf<WalletAddressInfo>()
        for (i in 0 until loopSize) {
            list.add(
                WalletAddressInfo(
                    address = addresses.getOrNull(i) ?: "",
                    primaryKey = keys.getOrNull(i) ?: ""
                )
            )
        }
        return list
    }
}
