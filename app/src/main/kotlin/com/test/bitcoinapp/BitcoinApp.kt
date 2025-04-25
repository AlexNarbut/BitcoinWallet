package com.test.bitcoinapp

import android.app.Application
import com.test.bitcoinappuikit.AppContext
import com.test.bitcoinappuikit.initContext
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow

@HiltAndroidApp
class BitcoinApp : Application(){

    override fun onCreate() {
        super.onCreate()

        initContext(
            MutableStateFlow(
                AppContext(
                    context = this.applicationContext
                )
            )
        )
    }
}