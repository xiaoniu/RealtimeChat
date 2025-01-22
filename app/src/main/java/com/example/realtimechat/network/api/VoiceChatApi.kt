package com.example.realtimechat.data.api

import com.example.realtimechat.network.data.StartVoiceChatRequest
import com.example.realtimechat.network.data.VoiceChatResponse
import com.example.realtimechat.network.data.StopVoiceChatRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface VoiceChatApi {
    @POST("?Action=StartVoiceChat&Version=2024-12-01")
    suspend fun startVoiceChat(
        @Body request: StartVoiceChatRequest
    ): VoiceChatResponse

    @POST("?Action=StopVoiceChat&Version=2024-12-01")
    suspend fun stopVoiceChat(
        @Body request: StopVoiceChatRequest
    ): VoiceChatResponse
}