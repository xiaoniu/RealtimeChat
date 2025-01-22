package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

class AgentConfig(
    @SerializedName("TargetUserId") val targetUserId: Array<String>,
    @SerializedName("WelcomeMessage") val welcomeMessage: String,
    @SerializedName("UserId") val userId: String
)