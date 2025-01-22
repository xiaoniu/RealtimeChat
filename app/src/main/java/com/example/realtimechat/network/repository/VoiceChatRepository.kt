package com.example.realtimechat.network.repository

import com.example.realtimechat.data.api.VoiceChatApi
import com.example.realtimechat.network.data.StartVoiceChatRequest
import com.example.realtimechat.network.data.StopVoiceChatRequest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VoiceChatRepository @Inject constructor(
    private val api: VoiceChatApi
) {
    fun startVoiceChat(request: StartVoiceChatRequest) = flow {
        emit(runCatching { api.startVoiceChat(request = request) })
    }

    fun stopVoiceChat(request: StopVoiceChatRequest) = flow {
        emit(runCatching { api.stopVoiceChat(request = request) })
    }
}