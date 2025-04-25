package com.test.bitcoinapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.test.wallet.currentstate.ui.CurrentStateScreen
import com.test.wallet.sendcoins.ui.SendCoinsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main,
        modifier = modifier
    ) {
        composable<Screen.Main> {
            CurrentStateScreen(
                modifier = modifier,
                onSendCoins = {
                    navController.navigate(
                        Screen.Send
                    )
                }
            )
        }

        composable<Screen.Send> {
            SendCoinsScreen(
                modifier = modifier,
                onBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
