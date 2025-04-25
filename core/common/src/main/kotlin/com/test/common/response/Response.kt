package com.test.common.response

import com.test.common.serialization.NotSerializable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
sealed class Response<out T> {
    @Serializable
    class Success<out T>(val value: T) : Response<T>()

    @Serializable(with = NotSerializable::class)
    sealed class Error : Response<Nothing>() {
        abstract val exception: Throwable
        abstract val message: String?
        fun getCode() = exception::class.simpleName

        @Serializable
        class Network(
            @Polymorphic
            override val exception: Throwable,
            override val message: String? = null,
        ) : Error()

        @Serializable
        class General(
            @Polymorphic
            override val exception: Throwable,
            @Serializable
            override val message: String? = null,
        ) : Error()
    }
}

enum class ResponseType {
    GENERAL,NETWORK
}
