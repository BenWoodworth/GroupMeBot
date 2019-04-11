package net.benwoodworth.groupme.api

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal fun String.urlEncode(): String {
    return URLEncoder.encode(this, Charsets.UTF_8.name())
}

internal fun buildUrl(
    url: String,
    params: Map<String, String?> = emptyMap()
): URL {
    val urlBuilder = StringBuilder(url)

    var firstParam = true
    for ((key, value) in params) {
        if (value != null) {
            if (firstParam) {
                urlBuilder.append('?')
                firstParam = false
            } else {
                urlBuilder.append('&')
            }

            urlBuilder.append(key.urlEncode())
            urlBuilder.append('=')
            urlBuilder.append(value.urlEncode())
        }
    }

    return URL(urlBuilder.toString())
}

internal suspend fun URL.request(
    method: String,
    headers: Map<String, List<String?>?> = emptyMap(),
    data: ByteArray? = null
): HttpResponse {
    return suspendCoroutine { continuation ->
        try {
            val connection = openConnection() as HttpURLConnection

            connection.requestMethod = method

            for ((key, values) in headers) {
                connection.setRequestProperty(key, null)
                values?.forEach { value ->
                    if (value != null) {
                        connection.addRequestProperty(key, value)
                    }
                }
            }

            if (data != null) {
                connection.doOutput = true
                connection.outputStream.use { outputStream ->
                    outputStream.write(data)
                }
            }

            connection.doInput = true
            connection.inputStream.use { reader ->
                val response = HttpResponse(
                    responseCode = connection.responseCode,
                    responseMessage = connection.responseMessage,
                    data = reader.readBytes()
                )

                continuation.resume(response)
            }
        } catch (t: Throwable) {
            continuation.resumeWithException(t)
        }
    }
}

class HttpResponse(
    val responseCode: Int,
    val responseMessage: String,
    val data: ByteArray
)
