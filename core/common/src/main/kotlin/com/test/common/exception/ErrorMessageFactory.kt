package com.test.common.exception

interface ErrorMessageFactory {
    fun create(exception: Throwable?, noMessage: Boolean = false): String
}