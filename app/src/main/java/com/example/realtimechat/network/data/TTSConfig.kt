package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class TTSConfig(
    @SerializedName("Provider") val provider: String,
    @SerializedName("ProviderParams") val providerParams: TTSProviderParams,
)
