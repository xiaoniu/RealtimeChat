package com.example.realtimechat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechat.network.data.ASRConfig
import com.example.realtimechat.network.data.ASRProviderParams
import com.example.realtimechat.network.data.AgentConfig
import com.example.realtimechat.network.data.LLMConfig
import com.example.realtimechat.network.data.StartVoiceChatRequest
import com.example.realtimechat.network.data.StopVoiceChatRequest
import com.example.realtimechat.network.data.TTSApp
import com.example.realtimechat.network.data.TTSConfig
import com.example.realtimechat.network.data.TTSProviderParams
import com.example.realtimechat.network.data.VoiceChatConfig
import com.example.realtimechat.network.repository.VoiceChatRepository
import com.ss.bytertc.engine.RTCRoom
import com.ss.bytertc.engine.RTCRoomConfig
import com.ss.bytertc.engine.RTCVideo
import com.ss.bytertc.engine.UserInfo
import com.ss.bytertc.engine.data.DataMessageSourceType
import com.ss.bytertc.engine.data.StreamIndex
import com.ss.bytertc.engine.handler.IRTCRoomEventHandler
import com.ss.bytertc.engine.handler.IRTCVideoEventHandler
import com.ss.bytertc.engine.type.ChannelProfile
import com.ss.bytertc.engine.type.LocalVideoStreamError
import com.ss.bytertc.engine.type.LocalVideoStreamState
import com.ss.bytertc.engine.type.MediaStreamType
import com.ss.bytertc.engine.type.RTCRoomStats
import com.ss.bytertc.engine.type.UserVisibilityChangeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VoiceChatViewModel @Inject constructor(
    private val repository: VoiceChatRepository,
    private val application: Application
) : ViewModel() {

    private val _isCalling = MutableStateFlow(false)
    val isCalling: StateFlow<Boolean> = _isCalling

    private val rtcVideo: RTCVideo by lazy {
        RTCVideo.createRTCVideo(application, Constants.APP_ID, rtcVideoListener, null, null)
    }
    private var rtcRoom: RTCRoom? = null

    private var taskId = ""


    private val rtcVideoListener = object : IRTCVideoEventHandler() {

        override fun onUserStartVideoCapture(roomId: String, uid: String) {
            super.onUserStartVideoCapture(roomId, uid)
            println("User $uid started video capture in room $roomId")
        }

        override fun onUserStopVideoCapture(roomId: String, uid: String) {
            super.onUserStopVideoCapture(roomId, uid)
            println("User $uid stopped video capture in room $roomId")
        }

        override fun onUserStartAudioCapture(roomId: String, uid: String) {
            super.onUserStartVideoCapture(roomId, uid)
            println("User $uid started Audio capture in room $roomId")
        }

        override fun onUserStopAudioCapture(roomId: String, uid: String) {
            super.onUserStopVideoCapture(roomId, uid)
            println("User $uid stopped Audio capture in room $roomId")
        }

        override fun onLocalVideoStateChanged(
            streamIndex: StreamIndex?,
            state: LocalVideoStreamState?,
            error: LocalVideoStreamError?
        ) {
            super.onLocalVideoStateChanged(streamIndex, state, error)
            println("onLocalVideoStateChanged")
        }

        override fun onCreateRoomStateChanged(roomId: String?, errorCode: Int) {
            super.onCreateRoomStateChanged(roomId, errorCode)
            println("onCreateRoomStateChanged:$roomId, $errorCode")
        }

        override fun onPublicStreamDataMessageReceived(
            publicStreamId: String?,
            message: ByteBuffer?,
            sourceType: DataMessageSourceType?
        ) {
            super.onPublicStreamDataMessageReceived(publicStreamId, message, sourceType)
            println("onPublicStreamDataMessageReceived")
        }

        override fun onWarning(warn: Int) {
            super.onWarning(warn)
            println("onWarning:$warn")
        }

        override fun onError(err: Int) {
            super.onError(err)
            println("onError:$err")
        }
    }

    private val rtcRoomListener = object : IRTCRoomEventHandler() {

        override fun onRoomStateChanged(
            roomId: String?,
            uid: String?,
            state: Int,
            extraInfo: String?
        ) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo)
            println("onRoomStateChanged:$roomId, $uid, $state, $extraInfo")
        }

        override fun onLeaveRoom(stats: RTCRoomStats?) {
            super.onLeaveRoom(stats)
            println("onLeaveRoom:$stats")
        }

        override fun onUserJoined(userInfo: UserInfo?, elapsed: Int) {
            super.onUserJoined(userInfo, elapsed)
            println("onUserJoined:$userInfo, $elapsed")
        }

        override fun onUserLeave(uid: String?, reason: Int) {
            super.onUserLeave(uid, reason)
            println("onUserLeave:$uid, $Int")
        }

        override fun onUserVisibilityChanged(
            currentUserVisibility: Boolean,
            errorCode: UserVisibilityChangeError?
        ) {
            super.onUserVisibilityChanged(currentUserVisibility, errorCode)
            println("onUserVisibilityChanged:$currentUserVisibility, $errorCode")
        }

        override fun onUserPublishStream(uid: String?, type: MediaStreamType?) {
            super.onUserPublishStream(uid, type)
            println("onUserPublishStream:$uid, $type")
        }
    }

    fun startCall() {
        // 开启音频采集
        rtcVideo.startAudioCapture()

        // 创建并加入房间
        rtcRoom = rtcVideo.createRTCRoom(Constants.ROOM_ID)
        rtcRoom?.setRTCRoomEventHandler(rtcRoomListener)

        val userInfo = UserInfo(Constants.USER_ID, "")
        val roomConfig = RTCRoomConfig(
            ChannelProfile.CHANNEL_PROFILE_CHAT_ROOM,
            true, // 自动发布
            true, // 自动订阅音频
            true  // 自动订阅视频
        )

        rtcRoom?.joinRoom(Constants.TOKEN, userInfo, roomConfig)

        // 启动语音机器人
        startAudioBot(Constants.ROOM_ID, Constants.USER_ID)
    }

    fun endCall() {
        // 停止音频采集
        rtcVideo.stopAudioCapture()

        // 离开并销毁房间
        rtcRoom?.leaveRoom()
        rtcRoom?.destroy()
        rtcRoom = null

        // 停止语音机器人
        stopAudioBot(Constants.ROOM_ID)
    }

    private fun startAudioBot(
        roomId: String,
        userId: String,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (isCalling.value) {
                return@launch
            }

            taskId = generateRandomTaskId()
            // 发起请求
            repository.startVoiceChat(
                StartVoiceChatRequest(
                    appId = Constants.APP_ID,
                    roomId = roomId,
                    taskId = taskId,
                    config = config,
                    agentConfig = AgentConfig(
                        targetUserId = arrayOf(userId),
                        welcomeMessage = "你好，我是火山引擎 RTC 语音助手，有什么需要帮忙的吗？",
                        userId = "BotName001"
                    )
                )
            ).collect { result ->
                result.onSuccess {
                    _isCalling.value = true
                    // 保存session信息
                }.onFailure { error ->
                    // 处理错误
                    _isCalling.value = false
                    println(error)
                }
            }
        } catch (e: Exception) {
            // 处理异常
            println(e)
        }
    }

    private fun stopAudioBot(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        if (isCalling.value) {
            try {
                repository.stopVoiceChat(
                    StopVoiceChatRequest(
                        appId = Constants.APP_ID,
                        roomId = roomId,
                        taskId = taskId
                    )
                ).collect { result ->
                    result.onSuccess {
                        _isCalling.value = false
                        // 清除session信息
                    }
                }
            } catch (e: Exception) {
                // 处理异常
                println(e)
            }
        }
    }

    private fun generateRandomTaskId(): String {
        // 使用 UUID 生成一个随机字符串
        val randomUUID = UUID.randomUUID().toString()
        // 如果需要确保长度不超过 128，可以截取一部分
        return randomUUID.replace("-", "").substring(0, 16)
    }

    override fun onCleared() {
        super.onCleared()
        RTCVideo.destroyRTCVideo()
    }

    companion object {
        val asrProviderParams = ASRProviderParams(
            mode = "smallmodel",
            appId = Constants.ASR_APP_ID,
            cluster = "volcengine_streaming_common"
        )

        val config = VoiceChatConfig(
            asrConfig = ASRConfig(
                provider = "volcano",
                providerParams = asrProviderParams
            ),
            ttsConfig = TTSConfig(
                provider = "volcano",
                providerParams = TTSProviderParams(
                    app = TTSApp(
                        appid = Constants.TTS_APP_ID,
                        cluster = "volcano_tts"
                    ),
                    resourceId = "volc.service_type.10029"
                )
            ),
            llmConfig = LLMConfig(
                mode = "ArkV3",
                endPointId = Constants.LLM_ENDPOINT_ID,
                maxTokens = 128,
                historyLength = 5
            ),
        )
    }

}