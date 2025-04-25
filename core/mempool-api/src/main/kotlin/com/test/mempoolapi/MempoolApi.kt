package com.test.mempoolapi

import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import com.test.mempoolapi.models.AddressInfoDTO
import com.test.mempoolapi.models.RecommendedFeeDTO
import com.test.mempoolapi.models.TransactionDTO
import com.test.mempoolapi.utils.PlainTextConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * [API Documentation](https://mempool.space/signet/docs/faq)
 */
interface MempoolApi {

    @GET("address/{address}")
    suspend fun getAddressInfo(@Path("address") address: String): Result<AddressInfoDTO>

    @GET("address/{address}/txs")
    suspend fun getAddressTransactions(@Path("address") address: String): Result<List<TransactionDTO>>

    @GET("v1/fees/recommended")
    suspend fun getRecommendedFees(): Result<RecommendedFeeDTO>

    @POST("tx")
    suspend fun sendTransaction(@Body transactionHex: String): Result<String>

}

fun getMempoolTransactionInfoUrl(tx: String): String {
    return "https://mempool.space/signet/tx/$tx"
}

private fun retrofit(baseUrl: String, okHttpClient: OkHttpClient, json: Json): Retrofit {
    val jsonConverterFactory = json.asConverterFactory("application/json".toMediaType())

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(PlainTextConverterFactory.create())
        .addConverterFactory(jsonConverterFactory)
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .build()
}

fun MempoolApi(
    baseUrl: String,
    okHttpClient: OkHttpClient,
    json: Json = Json
): MempoolApi {
    return retrofit(baseUrl, okHttpClient, json).create()
}

fun createOkHttpClient(): OkHttpClient {
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)

    return OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}