package com.test.bitcoinappuikit.lce

import com.test.common.response.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


sealed class LCEState<out T> {
    data object None : LCEState<Nothing>()
    data object Loading : LCEState<Nothing>()
    class Content<T>(val content: T) : LCEState<T>()
    class Error(val error: String) : LCEState<Nothing>()
}

fun <T, R> LCEState<T>.mapContent(mapper: (T) -> R): LCEState<R> {
    return when (this) {
        is LCEState.Content -> LCEState.Content(mapper(content))
        is LCEState.Error -> this
        is LCEState.Loading -> this
        is LCEState.None -> this
    }
}

suspend fun <R, L> MutableStateFlow<LCEState<L>>.applyFromExecuting(
    execute: suspend () -> Response<R>,
    needReload : Boolean = true,
    errorMapper: (Throwable) -> String,
    map: suspend (R) -> L
) {
    val currentState = this.value
    if(!currentState.isSuccess() || needReload){
        update { LCEState.Loading }
    }

    update {
        when (val response = execute()) {
            is Response.Success -> {
                val content = withContext(Dispatchers.Default) { map(response.value) }
                LCEState.Content(content)
            }

            is Response.Error -> createLCEError(errorMapper.invoke(response.exception))
        }
    }
}

fun createLCEError(error: String): LCEState.Error {
    return LCEState.Error(error)
}

inline fun <T> LCEState<T>.content(action: (T) -> Unit) {
    if (this is LCEState.Content) action(content)
}

inline fun <T> LCEState<T>.ifSuccess(action: (T) -> Unit) {
    if (this is LCEState.Content) action(content)
}

fun <T> LCEState<T>.isError(): Boolean {
    return this is LCEState.Error
}

fun <T> LCEState<T>.isSuccess(): Boolean {
    return this is LCEState.Content
}

fun <T> LCEState<T>.isFinalState(): Boolean {
    return this !is LCEState.Loading
}

fun <T> LCEState<T>.isLoading(): Boolean {
    return this is LCEState.Loading
}

fun <T> T.asLCEState() = LCEState.Content(this)

val <T> LCEState<T>.content: T?
    get() = if (this is LCEState.Content) content else null


