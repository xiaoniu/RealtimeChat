package com.example.realtimechat.network.interceptor

import android.annotation.SuppressLint
import com.example.realtimechat.network.utils.Sign
import okhttp3.Interceptor
import okhttp3.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone

class VoiceChatInterceptor : Interceptor {

    @SuppressLint("NewApi")
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 计算请求体SHA256
        val requestBody = originalRequest.body

        val query = originalRequest.url.query
        val queryMap = HashMap<String, String>()

        if (!query.isNullOrBlank()) {
            query.split("&").forEach { pair ->
                val parts = pair.split("=")
                if (parts.size == 2) {
                    queryMap[parts[0]] = parts[1]
                }
            }
        }

        val buffer = okio.Buffer()
        requestBody!!.writeTo(buffer)
        val bytes = buffer.readByteArray()

        val newRequest = Sign.signRequest(
            "POST",
            bytes,
            queryMap["Action"],
            "2024-12-01",
            originalRequest.newBuilder()
                .header("Host", originalRequest.url.host)
                .header("Content-Type", "application/json").build()
        )

        return chain.proceed(newRequest)
    }

}