package com.test.wallet.data.di

import com.test.mempoolapi.MempoolApi
import com.test.wallet.data.repository.interaction.WalletInteractionRepositoryImpl
import com.test.wallet.data.repository.profile.WalletProfileRepositoryImpl
import com.test.wallet.data.source.profile.WalletProfileRemoteFileDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import test.wallet.api.factory.BitcoinWalletFactory
import test.wallet.api.provider.WalletAddressInfosProvider
import test.wallet.api.repository.interaction.WalletInteractionRepository
import test.wallet.api.repository.profile.WalletProfileRepository
import java.util.UUID

@Module
@InstallIn(SingletonComponent::class)
object WalletModule {
    @Provides
    fun provideWalletProfileRemoteFileDataSource(walletAddressInfosProvider: WalletAddressInfosProvider): WalletProfileRemoteFileDataSource {
        return WalletProfileRemoteFileDataSource(
            UUID.randomUUID().toString(),
            walletAddressInfosProvider
        )
    }

    @Provides
    fun provideWalletProfileRepository(
        mempoolApi: MempoolApi,
        walletProfileRemoteFileDataSource: WalletProfileRemoteFileDataSource
    ): WalletProfileRepository {
        return WalletProfileRepositoryImpl(
            mempoolApi,
            walletProfileRemoteFileDataSource
        )
    }

    @Provides
    fun provideWalletInteractionRepository(
        bitcoinWalletFactory: BitcoinWalletFactory,
        profileRepository: WalletProfileRepository
    ): WalletInteractionRepository {
        return WalletInteractionRepositoryImpl(
            bitcoinWalletFactory,
            profileRepository
        )
    }
}
