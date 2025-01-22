package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class VoiceChatResponse(
    @SerializedName("Result") val result: String,
    @SerializedName("ResponseMetadata") val responseMetadata: ResponseMetadata
)