package test.transaction.api.repository

import com.test.common.response.Response
import test.transaction.api.model.RecommendedFee
import test.transaction.api.model.TransactionSendResult
import test.transaction.api.model.TransactionInfo
import test.transaction.api.repository.param.GetTransactionsRequest
import test.transaction.api.repository.param.SendTransactionRequest

interface TransactionRepository {
    suspend fun getAddressTransaction(param: GetTransactionsRequest): Response<List<TransactionInfo>>
    suspend fun getRecommendedFee(): Response<RecommendedFee>

    suspend fun send(param: SendTransactionRequest): Response<TransactionSendResult>
}