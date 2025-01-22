package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class StartVoiceChatRequest(
    @SerializedName("AppId") val appId: String,
    @SerializedName("RoomId") val roomId: String,
    @SerializedName("TaskId") val taskId: String,
    @SerializedName("Config") val config: VoiceChatConfig,
    @SerializedName("AgentConfig") val agentConfig: AgentConfig
)
