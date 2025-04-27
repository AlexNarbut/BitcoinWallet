package com.test.bitcoinapp.di

import android.content.Context
import com.test.bitcoinapp.exception.AppErrorMessageFactory
import com.test.bitcoinapp.provider.WalletAddressInfosAssertProvider
import com.test.common.AppDispatchers
import com.test.common.exception.ErrorMessageFactory
import com.test.mempoolapi.MempoolApi
import com.test.mempoolapi.createOkHttpClient
import com.test.transaction.data.builder.BitcoinjSignetTransactionBuilder
import com.test.wallet.data.factory.BitcoinSignetWalletFactoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import test.transaction.api.builder.TransactionBuilder
import test.transaction.api.repository.TransactionRepository
import test.wallet.api.factory.BitcoinWalletFactory
import test.wallet.api.provider.WalletAddressInfosProvider
import test.wallet.api.repository.profile.WalletProfileRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val BASE_URL = "https://mempool.space/signet/api/"

    private const val ADDRESSES_TXT_PATH = "addresses.txt"
    private const val KEYS_TXT_PATH = "keys.txt"

    @Provides
    @Singleton
    fun provideDefaultOkHttp(): OkHttpClient {
        return createOkHttpClient()
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    @Provides
    @Singleton
    fun provideMempoolApi(okHttpClient: OkHttpClient, json: Json): MempoolApi {
        return MempoolApi(
            baseUrl = BASE_URL,
            okHttpClient = okHttpClient,
            json = json
        )
    }

    @Provides
    @Singleton
    fun provideAppCoroutineDispatchers(): AppDispatchers = AppDispatchers()


    @Provides
    fun provideBitcoinWalletFactory(
        walletProfileRepository: WalletProfileRepository,
        transactionRepository: TransactionRepository,
        transactionBuilder: TransactionBuilder
    ): BitcoinWalletFactory {
        return BitcoinSignetWalletFactoryImpl(
            walletProfileRepository,
            transactionRepository,
            transactionBuilder
        )
    }

    @Provides
    fun provideWalletAddressInfosProvider(
        @ApplicationContext context: Context
    ): WalletAddressInfosProvider =
        WalletAddressInfosAssertProvider(
            context,
            ADDRESSES_TXT_PATH,
            KEYS_TXT_PATH
        )

    @Provides
    fun provideTransactionBuilder(
        transactionRepository: TransactionRepository,
    ): TransactionBuilder =
        BitcoinjSignetTransactionBuilder(
            transactionRepository
        )

    @Provides
    fun provideErrorMessageFactory(): ErrorMessageFactory = AppErrorMessageFactory()
}
