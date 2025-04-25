package com.test.bitcoinapp.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen{
    abstract val route : String

    @Serializable
    data object Main : Screen() {
        override val route: String = "Main"
    }

    @Serializable
    data object Send : Screen() {
        override val route: String = "Send"
    }
}