package com.example.realtimechat.network.data

import com.google.gson.annotations.SerializedName

data class StopVoiceChatRequest(
    @SerializedName("AppId") val appId: String,
    @SerializedName("RoomId") val roomId: String,
    @SerializedName("TaskId") val taskId: String
)