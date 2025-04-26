package com.test.bitcoinappuikit

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.test.bitcoinappuikit.theme.DimensImpl
import kotlinx.coroutines.flow.StateFlow

class AppContext(private val context: Context, val dimensImpl: DimensImpl) {
    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, formatArgs)
    }
}

private object ContextProvider {
    var _appAppContext: StateFlow<AppContext>? = null
    val appContext get() = _appAppContext!!
}

val LocalAppContext = staticCompositionLocalOf {
    ContextProvider.appContext.value
}

fun initContext(appContext: StateFlow<AppContext>) {
    ContextProvider._appAppContext = appContext
}

@Composable
fun composableString(resId: Int): String {
    return LocalAppContext.current.getString(resId)
}

fun string(resId: Int): String {
    return ContextProvider.appContext.value.getString(resId)
}

@Composable
fun composableString(resId: Int, vararg formatArgs: Any): String {
    return LocalAppContext.current.getString(resId, formatArgs)
}

fun string(resId: Int, vararg formatArgs: Any): String {
    return ContextProvider.appContext.value.getString(resId, formatArgs)
}

@Composable
fun composableDimens(): DimensImpl {
    return LocalAppContext.current.dimensImpl
}

val Dimens
    @Composable get() = LocalAppContext.current.dimensImpl

fun dimens(): DimensImpl {
    return ContextProvider.appContext.value.dimensImpl
}
