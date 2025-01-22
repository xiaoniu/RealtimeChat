package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class TTSApp(
    @SerializedName("appid") val appid: String? = null,
    @SerializedName("cluster") val cluster: String? = null
)