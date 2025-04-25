package com.test.commonextens

import android.util.Log

object Logger {
    inline fun d(tag: String? = null, msg: () -> String) {
        Log.d(tag, msg())
    }

    inline fun e(err: Throwable? = null, tag: String? = null, msg: () -> String?) {
        Log.e(tag, msg(), err)
    }

    inline fun e(tag: String? = null, msg: () -> String) {
        Log.e(tag, msg())
    }
}
