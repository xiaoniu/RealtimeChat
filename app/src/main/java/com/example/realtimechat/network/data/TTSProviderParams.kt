package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class TTSProviderParams(
    @SerializedName("app") val app: TTSApp? = null,
    @SerializedName("audio") val audio: BidirectionAudio? = null,
    @SerializedName("ResourceId") val resourceId: String? = null,
)