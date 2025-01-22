package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class VoiceChatConfig(
    @SerializedName("ASRConfig") val asrConfig: ASRConfig?,
    @SerializedName("TTSConfig") val ttsConfig: TTSConfig?,
    @SerializedName("LLMConfig") val llmConfig: LLMConfig?
)
