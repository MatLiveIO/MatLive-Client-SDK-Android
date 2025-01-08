package com.matnsolutions.matlive_sdk.audio.mangers

import android.util.Log
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRequestTakeMic
import com.matnsolutions.matlive_sdk.audio.seats.RoomSeatService
import com.matnsolutions.matlive_sdk.utils.kPrint
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets


class MatLiveRoomManger : LiveRoomEventManger() {
    companion object {
        val instance = MatLiveRoomManger()
    }

    var room: Room? = null


    override var inviteRequests: MutableStateFlow<List<MatLiveRequestTakeMic>> =
        MutableStateFlow(emptyList())
    override var seatService: RoomSeatService? = null
    private var _isSetUpped = false
    var onMic = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main)


    suspend fun setUp(
        onInvitedToMic: ((Int) -> Unit)?,
        onSendGift: ((String) -> Unit)?
    ) {
        if (room == null && _isSetUpped) return
        seatService = RoomSeatService()
        messages.value = emptyList()
        inviteRequests.value = emptyList()
        askPublish(false)

        coroutineScope.launch {
            room?.events?.collect { event ->
                kPrint("event: $event")
                when (event) {
                    is RoomEvent.ParticipantConnected -> {
                    }

                    is RoomEvent.ParticipantDisconnected -> {
                    }

                    is RoomEvent.TrackPublished -> {
                    }

                    is RoomEvent.TrackUnpublished -> {
                    }

                    is RoomEvent.TrackSubscribed -> {
                    }

                    is RoomEvent.TrackUnsubscribed -> {
                    }

                    is RoomEvent.TrackStreamStateChanged -> {
//                        kPrint(event.toString())
                        //                        if (!room.canPlaybackAudio) {
                        //                            kPrint("AudioPlaybackStatusChanged Audio playback failed")
                        //                            scope.launch {
                        //                                room.startAudio()
                        //                            }
                        //                        }
                    }

                    is RoomEvent.DataReceived -> {
                        val data = try {
                            val jsonString = String(event.data, StandardCharsets.UTF_8)
                            val jsonObject = JSONObject(jsonString)
                            val map = mutableMapOf<String, Any>()

                            // Convert JSONObject to Map<String, String>
                            for (key in jsonObject.keys()) {
                                // Get value as string, handling potential null values
                                val value = jsonObject.opt(key) ?: continue
                                map[key] = value
                            }
                            map
                        } catch (e: Exception) {
                            mapOf()
                        }
                        kPrint(data)
                        receivedData(
                            data,
                            onInvitedToMic,
                            onSendGift
                        )
                    }

                    is RoomEvent.Disconnected -> {
//                        kPrint("RoomDisconnectedEvent ${event.reason}")
                    }

                    is RoomEvent.RecordingStatusChanged -> {
                    }

                    is RoomEvent.RoomMetadataChanged -> {
                        if (event.newMetadata != null &&
                            event.newMetadata!!.isNotEmpty() &&
                            event.newMetadata!!.contains("seats")
                        ) {
                            coroutineMainScope.launch {
                                seatService?.seatsFromMetadata(event.newMetadata)
                            }
                        }
                    }

                    is RoomEvent.LocalTrackSubscribed -> {
                    }

                    is RoomEvent.ParticipantNameChanged -> {
                    }

                    is RoomEvent.ParticipantMetadataChanged -> {
                    }

                    is RoomEvent.TrackE2EEStateEvent -> {
                        kPrint("e2ee state: ${event.state}")
                    }

                    is RoomEvent.Reconnecting -> {
                    }

                    else -> {
                    }
                }
            }
        }


        _isSetUpped = true
    }

    suspend fun close() {
        askPublish(false);
        _isSetUpped = false
        onMic = false;
        seatService?.clear()
        messages.value = emptyList()
        inviteRequests.value = emptyList()
//        listener?.dispose()
        room?.disconnect()
        room?.release()
        room = null
    }

    suspend fun takeSeat(seatIndex: Int) {
        askPublish(true)
        onMic = true;
        seatService?.takeSeat(
            seatIndex,
            MatLiveJoinRoomManger.instance.currentUser!!
        )
    }

    suspend fun lockSeat(seatIndex: Int) {
        seatService?.lockSeat(seatIndex)
    }

    suspend fun unLockSeat(seatIndex: Int) {
        seatService?.unLockSeat(seatIndex)
    }

    suspend fun leaveSeat(seatIndex: Int) {
        onMic = false;
        askPublish(false)
        seatService?.leaveSeat(
            seatIndex,
            MatLiveJoinRoomManger.instance.currentUser!!.userId
        )
    }

    override suspend fun sendMessage(message: String) {
        messages.value += MatLiveChatMessage(
            roomId = MatLiveJoinRoomManger.instance.roomId,
            user = MatLiveJoinRoomManger.instance.currentUser!!,
            message = message,
        )
        super.sendMessage(message)
    }

    suspend fun muteSeat(seatIndex: Int) {
        askPublishMute(true)
        Log.e("Mute", "muteSeat")
        seatService?.muteSeat(seatIndex)
    }

    suspend fun unMuteSeat(seatIndex: Int) {
        kPrint("unMuteSeat")
        askPublishMute(false)
        seatService?.unMuteSeat(seatIndex)
    }

    suspend fun removeUserFromSeat(seatIndex: Int) {
        kPrint("removeUserFromSeat")
        val userId = seatService?.removeUserFromSeat(seatIndex)
        if (userId != null) {
            super.removeSpeaker(seatIndex, userId)
        }
    }

    suspend fun switchSeat(toSeatIndex: Int) {
        val userId = MatLiveJoinRoomManger.instance.currentUser!!.userId
        val seatId =
            seatService?.seatList?.value?.indexOfFirst { it.currentUser.value?.userId == userId }
        if (seatId == null || seatId == -1) return
        seatService?.switchSeat(
            seatId,
            toSeatIndex,
            userId
        )
        kPrint("switchSeat")
    }

    private suspend fun askPublishMute(value: Boolean) {
        if (value) {
            room?.localParticipant?.setMicrophoneEnabled(false)
        } else {
            room?.localParticipant?.setMicrophoneEnabled(true)
        }
    }

    private suspend fun askPublish(value: Boolean) {
        room?.localParticipant?.setMicrophoneEnabled(value)
        room?.localParticipant?.setCameraEnabled(false)
    }
}