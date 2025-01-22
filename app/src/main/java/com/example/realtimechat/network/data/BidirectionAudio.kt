package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class BidirectionAudio(
    @SerializedName("voice_type") val voiceType: String? = null,
    @SerializedName("pitch_rate") val pitchRate: Int? = 0,
    @SerializedName("speech_rate") val speechRate: Int? = 0,
)