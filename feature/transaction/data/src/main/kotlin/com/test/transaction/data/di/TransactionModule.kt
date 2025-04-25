package com.test.transaction.data.di

import com.test.mempoolapi.MempoolApi
import com.test.transaction.data.repository.TransactionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import test.transaction.api.repository.TransactionRepository

@Module
@InstallIn(SingletonComponent::class)
object TransactionModule {
    @Provides
    fun provideTransactionRepository(mempoolApi: MempoolApi): TransactionRepository {
        return TransactionRepositoryImpl(mempoolApi)
    }
}
