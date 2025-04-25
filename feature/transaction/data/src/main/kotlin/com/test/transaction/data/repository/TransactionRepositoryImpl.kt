package com.test.transaction.data.repository

import com.test.common.response.Response
import com.test.common.response.ResponseType
import com.test.commonextens.response.mapValueIfSuccess
import com.test.commonextens.response.toResponse
import com.test.mempoolapi.MempoolApi
import com.test.mempoolapi.getMempoolTransactionInfoUrl
import com.test.transaction.data.mapper.toDomain
import test.transaction.api.model.TransactionInfo
import test.transaction.api.model.TransactionSendResult
import test.transaction.api.repository.TransactionRepository
import test.transaction.api.repository.param.GetTransactionsRequest
import test.transaction.api.repository.param.SendTransactionRequest


class TransactionRepositoryImpl(
    private val mempoolApi: MempoolApi,
) : TransactionRepository {

    override suspend fun getAddressTransaction(param: GetTransactionsRequest): Response<List<TransactionInfo>> {
        return mempoolApi.getAddressTransactions(param.address).toResponse(ResponseType.NETWORK)
            .mapValueIfSuccess { list ->
                list.mapNotNull { item ->
                    item.toDomain(infoUrlProvider = {
                        getMempoolTransactionInfoUrl(
                            item.txId ?: ""
                        )
                    })
                }
            }
    }

    override suspend fun send(param: SendTransactionRequest): Response<TransactionSendResult> {
        return mempoolApi.sendTransaction(param.transactionHex).toResponse(ResponseType.NETWORK)
            .mapValueIfSuccess { txid ->
                TransactionSendResult(
                    txid,
                    getMempoolTransactionInfoUrl(txid)
                )
            }
    }
}