package com.matnsolutions.matlive_sdk.audio.mangers

import android.content.Context
import android.util.Log
import com.matnsolutions.matlive_sdk.audio.define.JoinRequest
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRequestTakeMic
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser
import com.matnsolutions.matlive_sdk.services.MatLiveService
import com.matnsolutions.matlive_sdk.services.Utils
import com.matnsolutions.matlive_sdk.utils.kPrint
import io.livekit.android.AudioOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.audio.AudioProcessorOptions
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.e2ee.BaseKeyProvider
import io.livekit.android.e2ee.E2EEOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets


class MatLiveRoomManger private constructor() : LiveRoomEventManger() {
    companion object {
        val instance: MatLiveRoomManger by lazy { MatLiveRoomManger() }
    }

    private val matLiveService = MatLiveService()
    private val _request = JoinRequest()

    var currentUser: MatLiveUser? = null
    var roomId: String = ""
    var room: Room? = null

    override var inviteRequests: MutableStateFlow<List<MatLiveRequestTakeMic>> =
        MutableStateFlow(emptyList())
    private var _isSetUpped = false
    var onMic = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main)

    private var onInvitedToMic: ((Int) -> Unit)? = null
    private var onSendGift: ((String) -> Unit)? = null

    suspend fun connect(
        context: Context,
        appKey: String,
        name: String,
        avatar: String,
        userId: String,
        roomId: String,
        metadata: String? = null,
        onInvitedToMic: ((Int) -> Unit)?,
        onSendGift: ((String) -> Unit)?,
    ) {

        _request.url = Utils.url
        this.onInvitedToMic = onInvitedToMic
        this.onSendGift = onSendGift

        joinRoom(roomId, appKey) { newId, token ->
            _request.token = token
            this.roomId = newId
        }
        if (_request.token.isEmpty()) return


        currentUser = MatLiveUser(
            name = name,
            avatar = avatar,
            userId = userId,
            roomId = userId,
            metadata = metadata,
        )
        try {
            var e2eeOptions: E2EEOptions? = null
            if (_request.e2ee && _request.e2eeKey != null) {
                val keyProvider = BaseKeyProvider()
                e2eeOptions = E2EEOptions(keyProvider = keyProvider)
                keyProvider.setKey(key = _request.e2eeKey, participantId = userId)
            }

            room = LiveKit.create(
                appContext = context,
                options = RoomOptions(
                    adaptiveStream = _request.adaptiveStream,
                    dynacast = _request.dynacast,
                    e2eeOptions = e2eeOptions,
//                    audioTrackPublishDefaults = AudioTrackPublishDefaults(audioBitrate = 3200)
                ),
                overrides = LiveKitOverrides(
                    audioOptions = AudioOptions(audioProcessorOptions = AudioProcessorOptions()),
                ),
            )
            room?.prepareConnection(_request.url, _request.token)

            coroutineScope.launch {
                room?.connect(
                    _request.url,
                    _request.token,
                )
            }
            setUp(onInvitedToMic, onSendGift)

            delay(1000)
            val audioHandler = room?.audioHandler as? AudioSwitchHandler
            audioHandler?.let {
                it.selectDevice(it.availableAudioDevices.last())
            }
        } catch (error: Exception) {
            kPrint("Could not connect $error")
            throw Exception("Failed to update metadata: $error")
        }
    }

    private suspend fun setUp(
        onInvitedToMic: ((Int) -> Unit)?,
        onSendGift: ((String) -> Unit)?
    ) {
        if (room == null && _isSetUpped) return
        messages.value = emptyList()
        inviteRequests.value = emptyList()
        setMicrophoneEnabled(false)

        coroutineScope.launch {
            room?.events?.collect { event ->
                kPrint("event: ${event.toString().split(".").last()}")
                when (event) {
                    is RoomEvent.DataReceived -> {
                        val data = try {
                            val jsonString = String(event.data, StandardCharsets.UTF_8)
                            val jsonObject = JSONObject(jsonString)
                            val map = mutableMapOf<String, Any>()

                            for (key in jsonObject.keys()) {
                                val value = jsonObject.opt(key) ?: continue
                                map[key] = value
                            }
                            map
                        } catch (e: Exception) {
                            mapOf()
                        }
                        receivedData(
                            data,
                            onInvitedToMic,
                            onSendGift
                        )
                    }
                    is RoomEvent.RoomMetadataChanged -> {
                        if (event.newMetadata != null &&
                            event.newMetadata!!.isNotEmpty() &&
                            event.newMetadata!!.contains("seats")
                        ) {
                            coroutineMainScope.launch {
                                seatService.seatsFromMetadata(event.newMetadata)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
        _isSetUpped = true
    }

    fun close() {
        CoroutineScope(Dispatchers.Main).launch {
            setMicrophoneEnabled(false)
            _isSetUpped = false
            onMic = false
            seatService.clear()
            messages.value = emptyList()
            inviteRequests.value = emptyList()
            room?.disconnect()
            room?.release()
            room = null
        }

    }

    suspend fun takeSeat(seatIndex: Int) {
        setMicrophoneEnabled(true)
        onMic = true
        seatService.takeSeat(
            seatIndex,
            instance.currentUser!!
        )
    }

    suspend fun lockSeat(seatIndex: Int) {
        seatService.lockSeat(seatIndex)
    }

    suspend fun unLockSeat(seatIndex: Int) {
        seatService.unLockSeat(seatIndex)
    }

    suspend fun leaveSeat(seatIndex: Int) {
        onMic = false
        setMicrophoneEnabled(false)
        seatService.leaveSeat(
            seatIndex,
            instance.currentUser!!.userId
        )
    }

    override suspend fun sendMessage(message: String) {
        messages.value += MatLiveChatMessage(
            roomId = this.roomId,
            user = currentUser!!,
            message = message,
        )
        super.sendMessage(message)
    }

    suspend fun muteSeat(seatIndex: Int) {
        val userSeatId =
            seatService.muteSeat(seatIndex) ?: return
        if(currentUser?.userId == userSeatId){
            setMicrophoneEnabled(false)
        }else{
            super.muteSeat(userSeatId,seatIndex)
        }
        seatService.muteSeat(seatIndex)
    }

    suspend fun unMuteSeat(seatIndex: Int) {
        val userSeatId =
            seatService.unMuteSeat(seatIndex) ?: return
        if(currentUser?.userId == userSeatId){
            setMicrophoneEnabled(true)
        }else{
            super.unMuteSeat(userSeatId,seatIndex)
        }
    }

    suspend fun removeUserFromSeat(seatIndex: Int) {
        val userId = seatService.removeUserFromSeat(seatIndex)
        if (userId != null) {
            super.removeSpeaker(seatIndex, userId)
        }
    }

    suspend fun switchSeat(toSeatIndex: Int) {
        val userId = currentUser!!.userId
        val seatId =
            seatService.seatList.value.indexOfFirst { it.currentUser.value?.userId == userId }
        if (seatId == -1) return
        seatService.switchSeat(
            seatId,
            toSeatIndex,
            userId
        )
    }



    private suspend fun setMicrophoneEnabled(isMute: Boolean) {
        room?.localParticipant?.setMicrophoneEnabled(isMute)
    }

    private suspend fun joinRoom(
        roomId: String,
        appKey: String,
        onSuccess: (String, String) -> Unit
    ) {
        try {
            val tokenResponse = matLiveService.createToken(
                username = "user_${System.currentTimeMillis()}",
                roomId = roomId,
                appKey = appKey
            )
            tokenResponse.onSuccess { response ->
                // Handle success
                val data = response["data"] as Map<*, *>
                val token = data["token"] as String
                val newId = data["newRoomName"] as String
                println("joinRoom: id $roomId, token $token")
                onSuccess(newId, token)
            }.onFailure { error ->
                // Handle error
                Log.e("joinRoom", "joinRoom error: $error")
            }
        } catch (e: Exception) {
            Log.e("joinRoom", "joinRoom error: $e")
        }
    }

}