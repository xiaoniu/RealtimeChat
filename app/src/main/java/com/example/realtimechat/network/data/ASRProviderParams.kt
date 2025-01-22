package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class ASRProviderParams(
    @SerializedName("Mode") val mode: String?,
    @SerializedName("AppId") val appId: String?,
    @SerializedName("Cluster") val cluster: String?
)