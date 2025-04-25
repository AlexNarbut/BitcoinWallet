package com.test.bitcoinappuikit.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.commonextens.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {
    protected open val errorHandler = CoroutineExceptionHandler { context, exception ->
        Logger.e(exception, LOG_TAG) { exception.stackTraceToString() }
    }


    inline fun e(err: Throwable? = null, tag: String? = null, msg: () -> String?) {
        Log.e(tag, msg(), err)
    }

    protected open fun processCriticalError(exception: Throwable) {}

    protected fun safeLaunch(
        coroutineContext: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = if (coroutineContext[CoroutineExceptionHandler.Key] != null) {
            coroutineContext
        } else {
            coroutineContext + errorHandler
        }
        return viewModelScope.launch(context) {
            block.invoke(this)
        }
    }

    protected suspend fun launchOnMain(block: () -> Unit) = withContext(Dispatchers.Main) {
        viewModelScope.launch {
            block.invoke()
        }
    }

    companion object {
        private const val LOG_TAG = "ViewModel"
    }
}