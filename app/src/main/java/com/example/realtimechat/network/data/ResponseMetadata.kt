package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

class ResponseMetadata(
    @SerializedName("RequestId") val requestId: String,
    @SerializedName("Action") val action: String,
    @SerializedName("Version") val version: String,
    @SerializedName("Service") val service: String,
    @SerializedName("Region") val region: String
)