package com.example.realtimechat.network.di

import com.example.realtimechat.data.api.VoiceChatApi
import com.example.realtimechat.network.interceptor.VoiceChatInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 定义基础URL常量
    private const val API_BASE_URL = "https://rtc.volcengineapi.com"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // 创建信任所有证书的TrustManager
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // 创建SSLContext
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        // 创建HostnameVerifier
        val hostnameVerifier = HostnameVerifier { _, _ -> true }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(hostnameVerifier)
            .addInterceptor(VoiceChatInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideVoiceChatApi(okHttpClient: OkHttpClient): VoiceChatApi {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VoiceChatApi::class.java)
    }
}