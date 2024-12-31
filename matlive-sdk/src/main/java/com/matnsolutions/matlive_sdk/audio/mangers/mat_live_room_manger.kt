package com.matnsolutions.matlive_sdk.audio.mangers

import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRequestTakeMic
import com.matnsolutions.matlive_sdk.audio.seats.RoomSeatService
import com.matnsolutions.matlive_sdk.utils.kPrint
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets

//enum class ParticipantTrackType {
//    kUserMedia,
//    kScreenShare
//}
//
//data class ParticipantTrack(
//    val participant: Participant,
//    val type: ParticipantTrackType = ParticipantTrackType.kUserMedia
//)


class MatLiveRoomManger : LiveRoomEventManger() {
    companion object {
        val instance = MatLiveRoomManger()
    }

    var room: Room? = null

    //    private var listener: EventCollector<RoomEvent>? = null
//    var participantTracks = mutableListOf<ParticipantTrack>()

    //    override var messages: MutableStateFlow<List<MatLiveChatMessage>> =
//        MutableStateFlow(emptyList())
    override var inviteRequests: MutableStateFlow<List<MatLiveRequestTakeMic>> =
        MutableStateFlow(emptyList())
    override var seatService: RoomSeatService? = null
    private var _flagStartedReplayKit = false
    private var _isSetUpped = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main)

//    val fastConnection: Boolean
//        get() = room?.engine?.fastConnectOptions != null

    suspend fun setUp(
        onInvitedToMic: ((Int) -> Unit)?,
        onSendGift: ((String) -> Unit)?
    ) {
        if (room == null && _isSetUpped) return
        seatService = RoomSeatService()
        messages.value = emptyList()
        inviteRequests.value = emptyList()
        _askPublish(false)

        coroutineScope.launch {
            room?.events?.collect { event ->
                kPrint("event: $event")
                when (event) {
                    is RoomEvent.ParticipantConnected -> {
                        _sortParticipants()
                    }

                    is RoomEvent.ParticipantDisconnected -> {
                        _sortParticipants()
                    }

                    is RoomEvent.TrackPublished -> {
                        _sortParticipants()
                    }

                    is RoomEvent.TrackUnpublished -> {
                        _sortParticipants()
                    }

                    is RoomEvent.TrackSubscribed -> {
                        _sortParticipants()
                    }

                    is RoomEvent.TrackUnsubscribed -> {
                        _sortParticipants()
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
//                        kPrint(event.toString())
                    }

                    is RoomEvent.RoomMetadataChanged -> {
//                        kPrint("RoomMetadataChangedEvent ${event.newMetadata}")
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
                        _sortParticipants()
                    }

                    is RoomEvent.ParticipantNameChanged -> {
//                        kPrint("ParticipantNameUpdatedEvent")
                        _sortParticipants()
                    }

                    is RoomEvent.ParticipantMetadataChanged -> {
//                        kPrint("ParticipantMetadataUpdatedEvent")
                    }

                    is RoomEvent.TrackE2EEStateEvent -> {
                        kPrint("e2ee state: ${event.state}")
                    }

                    is RoomEvent.Reconnecting -> {
//                        kPrint("RoomAttemptReconnectEvent ")
                    }

                    else -> {
//                        kPrint("unhandled event: $event")
                    }
                }
            }
        }

        _sortParticipants()

//        if (LiveKit.platformType == LiveKit.PlatformType.ANDROID) {
//            // Hardware.instance.setSpeakerphoneOn(true) // TODO: Implement Hardware class
//        }

        _isSetUpped = true
    }

    suspend fun close() {
        _isSetUpped = false
        seatService?.clear()
        messages.value = emptyList()
        inviteRequests.value = emptyList()
//        listener?.dispose()
        room?.disconnect()
        room?.release()
        room = null
    }

    suspend fun takeSeat(seatIndex: Int) {
        _askPublish(true)
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
        _askPublish(false)
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
        _askPublishMute(true)
        seatService?.muteSeat(seatIndex)
    }

    suspend fun unMuteSeat(seatIndex: Int) {
        kPrint("unMuteSeat")
        _askPublishMute(false)
        seatService?.unMuteSeat(seatIndex)
    }

    suspend fun removeUserFromSeat(seatIndex: Int) {
        kPrint("removeUserFromSeat")
        seatService?.removeUserFromSeat(seatIndex)
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

    private suspend fun _askPublishMute(value: Boolean) {
        if (value) {
            room?.localParticipant?.setMicrophoneEnabled(false)
        } else {
            room?.localParticipant?.setMicrophoneEnabled(true)
        }
    }

    private suspend fun _askPublish(value: Boolean) {
        try {
            if (value) {
                MatLiveJoinRoomManger.instance.audioTrack?.start()
            } else {
                MatLiveJoinRoomManger.instance.audioTrack?.stop()
            }
        } catch (error: Exception) {
            kPrint("could not publish audio: $error")
        }
        room?.localParticipant?.setMicrophoneEnabled(value)
        room?.localParticipant?.setCameraEnabled(false)
    }

    private fun _sortParticipants() {
//        val userMediaTracks = mutableListOf<ParticipantTrack>()
//        room?.let { room ->
//            for (participant in room.remoteParticipants.values) {
//                for (trackPublication in participant.videoTracks) {
//                    if (!trackPublication.isScreenShare) {
//                        userMediaTracks.add(ParticipantTrack(participant = participant))
//                    }
//                }
//            }
//
//            userMediaTracks.sortWith(compareBy<ParticipantTrack> {
//                if (it.participant.isSpeaking) {
//                    -it.participant.audioLevel
//                } else {
//                    0f
//                }
//            }.thenComparing { a, b ->
//                val aSpokeAt = a.participant.lastSpokeAt ?: 0
//                val bSpokeAt = b.participant.lastSpokeAt ?: 0
//                when {
//                    aSpokeAt != bSpokeAt -> -aSpokeAt.compareTo(bSpokeAt)
//                    a.participant.videoTrackPublications.isNotEmpty() != b.participant.videoTrackPublications.isNotEmpty() -> if (a.participant.videoTrackPublications.isNotEmpty()) -1 else 1
//                    else -> a.participant.joinedAt!!.compareTo(b.participant.joinedAt!!)
//                }
//            })
//
//            val localParticipantTracks = room.localParticipant?.videoTrackPublishDefaults
//            localParticipantTracks?.let { tracks ->
//                for (trackPublication in tracks) {
//                    if (trackPublication.isScreenShare) {
////                        if (LiveKit.platformType == LiveKit.PlatformType.IOS) {
////                            if (!_flagStartedReplayKit) {
////                                _flagStartedReplayKit = true
////                                // ReplayKitChannel.startReplayKit() // TODO: Implement ReplayKitChannel
////                            }
////                        }
//                    } else {
////                        if (LiveKit.platformType == LiveKit.PlatformType.IOS) {
////                            if (_flagStartedReplayKit) {
////                                _flagStartedReplayKit = false
////                                // ReplayKitChannel.closeReplayKit() // TODO: Implement ReplayKitChannel
////                            }
////                        }
//                        room.localParticipant?.let {
//                            userMediaTracks.add(ParticipantTrack(participant = it))
//                        }
//                    }
//                }
//            }
//            participantTracks = userMediaTracks
//        }
    }
}