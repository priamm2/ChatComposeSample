package com.example.chatcomposesample.android.client.errors

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import com.example.chatcomposesample.result.Error
import io.getstream.chat.android.client.errors.ChatErrorCode
import java.net.UnknownHostException

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_TIMEOUT = 408
private const val HTTP_API_ERROR = 500

@InternalStreamChatApi
fun Error.NetworkError.Companion.fromChatErrorCode(
    chatErrorCode: ChatErrorCode,
    statusCode: Int = UNKNOWN_STATUS_CODE,
    cause: Throwable? = null,
): Error.NetworkError {
    return Error.NetworkError(
        message = chatErrorCode.description,
        serverErrorCode = chatErrorCode.code,
        statusCode = statusCode,
        cause = cause,
    )
}

@InternalStreamChatApi
fun Error.isPermanent(): Boolean {
    return if (this is Error.NetworkError) {
        val temporaryErrors = listOf(HTTP_TOO_MANY_REQUESTS, HTTP_TIMEOUT, HTTP_API_ERROR)

        when {
            statusCode in temporaryErrors -> false
            cause is UnknownHostException -> false
            else -> true
        }
    } else {
        false
    }
}

@InternalStreamChatApi
fun Error.copyWithMessage(message: String): Error {
    return when (this) {
        is Error.GenericError -> this.copy(message = message)
        is Error.NetworkError -> this.copy(message = message)
        is Error.ThrowableError -> this.copy(message = message)
    }
}

@InternalStreamChatApi
public fun Error.extractCause(): Throwable? {
    return when (this) {
        is Error.GenericError -> null
        is Error.NetworkError -> cause
        is Error.ThrowableError -> cause
    }
}
