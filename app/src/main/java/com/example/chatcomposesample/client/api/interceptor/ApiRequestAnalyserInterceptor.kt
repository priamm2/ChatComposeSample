package com.example.chatcomposesample.client.api.interceptor

import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.models.Constants
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.OutputStream

internal class ApiRequestAnalyserInterceptor(private val requestsAnalyser: ApiRequestsAnalyser) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val buffer = Buffer()
        val stringOutputStream = StringOutputStream()

        chain.request().body?.writeTo(buffer)
        writeRequestBody(stringOutputStream, buffer)

        requestsAnalyser.registerRequest(request.url.toString(), mapOf("body" to stringOutputStream.toString()))

        return chain.proceed(request)
    }

    @StreamHandsOff(
        reason = "Request body shouldn't be written entirely as it might produce OutOfMemory " +
            "exceptions when sending big files." +
            " The output will be limited to ${Constants.MAX_REQUEST_BODY_LENGTH} bytes.",
    )
    private fun writeRequestBody(stringOutputStream: StringOutputStream, buffer: Buffer) {
        buffer.writeTo(stringOutputStream, minOf(buffer.size, Constants.MAX_REQUEST_BODY_LENGTH))
    }
}

private class StringOutputStream : OutputStream() {

    private val stringBuilder = StringBuilder()

    override fun write(b: Int) {
        stringBuilder.append(b.toChar())
    }

    override fun toString(): String = stringBuilder.toString().ifEmpty { "no_body" }
}
