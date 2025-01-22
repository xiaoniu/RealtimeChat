package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class LLMConfig(
    @SerializedName("Mode") val mode: String = "ArkV3",
    @SerializedName("EndPointId") val endPointId: String,
    @SerializedName("MaxTokens") val maxTokens: Int,
    @SerializedName("HistoryLength") val historyLength: Int,

)
