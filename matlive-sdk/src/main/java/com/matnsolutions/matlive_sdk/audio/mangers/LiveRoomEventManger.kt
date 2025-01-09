package com.matnsolutions.matlive_sdk.audio.mangers

import android.util.Log
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRequestTakeMic
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.define.MatLiveEvents
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser
import com.matnsolutions.matlive_sdk.audio.seats.RoomSeatService
import com.matnsolutions.matlive_sdk.utils.kPrint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.nio.charset.StandardCharsets

open class LiveRoomEventManger {
    open var seatService: RoomSeatService? = null

    open val messages: MutableStateFlow<List<MatLiveChatMessage>> =
        MutableStateFlow(emptyList())
    private val _inviteRequests = MutableStateFlow<List<MatLiveRequestTakeMic>>(emptyList())
    open val inviteRequests: StateFlow<List<MatLiveRequestTakeMic>> = _inviteRequests.asStateFlow()


    suspend fun receivedData(
        data: Map<String, Any>,
        onInvitedToMic: ((Int) -> Unit)? = null,
        onSendGift: ((String) -> Unit)? = null
    ) {
        if (seatService == null) return
        try {
            val event = data["event"] as Int
            val user = data["user"] as JSONObject
            val matUser = MatLiveUser(
                userId = user["userId"] as String,
                name = user["name"] as String,
                avatar = user["avatar"] as String,
                roomId = data["roomId"] as String,
                metadata = data["metadata"] as String?
            )
            when (event) {
                MatLiveEvents.sendMessage -> {
                    messages.value += MatLiveChatMessage(
                        user = matUser,
                        message = data["message"] as String,
                        roomId = data["roomId"] as String
                    )
                }

                MatLiveEvents.removeUserFromSeat -> {
                    if (data["userId"] == MatLiveJoinRoomManger.instance.currentUser?.userId) {
                        MatLiveJoinRoomManger.instance.audioTrack?.stop()
                        MatLiveJoinRoomManger.instance.room?.localParticipant?.setMicrophoneEnabled(
                            false
                        )
                        MatLiveJoinRoomManger.instance.onMic = false
                    }
                }

                MatLiveEvents.clearChat -> {
                    messages.value = emptyList()
                }

                MatLiveEvents.inviteUserToTakeMic -> {
                    if (MatLiveJoinRoomManger.instance.currentUser?.userId == data["userId"] && onInvitedToMic != null) {
                        onInvitedToMic(data["seatIndex"] as Int)
                    }
                }

                MatLiveEvents.sendGift -> {
                    onSendGift?.invoke(data["gift"] as String)
                }

                MatLiveEvents.requestTakeMic -> {
                    _inviteRequests.value += MatLiveRequestTakeMic(
                        user = matUser,
                        seatIndex = data["seatIndex"] as Int
                    )
                }
            }
        } catch (e: Exception) {
            kPrint("Error handling data received: $e")
            val stackTraceString = e.stackTraceToString()
            Log.e("MyTag", "Error: $stackTraceString")
        }
    }


    private suspend fun publish(data: Map<String, Any>) {
        val room = MatLiveJoinRoomManger.instance.room
        if (room != null) {
            val jsonObject = JSONObject(data)

            // Add user data
            val userObject = JSONObject().apply {
                put("userId", MatLiveJoinRoomManger.instance.currentUser?.userId)
                put("name", MatLiveJoinRoomManger.instance.currentUser?.name)
                put("avatar", MatLiveJoinRoomManger.instance.currentUser?.avatar)
            }
            jsonObject.put("user", userObject)

            // Add room ID
            jsonObject.put("roomId", room.name.toString())

            // Convert to byte array and publish
            val decoded = jsonObject.toString().toByteArray(StandardCharsets.UTF_8)
            room.localParticipant.publishData(decoded)
        }
    }

    open suspend fun sendMessage(message: String) {
        publish(
            mapOf(
                "event" to MatLiveEvents.sendMessage,
                "message" to message
            )
        )
    }

    suspend fun sendGift(gift: String) {
        publish(
            mapOf(
                "event" to MatLiveEvents.sendGift,
                "gift" to gift
            )
        )
    }

    suspend fun clearChat() {
        publish(
            mapOf(
                "event" to MatLiveEvents.clearChat
            )
        )
    }

    suspend fun inviteUserToTakeMic(userId: String, seatIndex: Int) {
        publish(
            mapOf(
                "event" to MatLiveEvents.inviteUserToTakeMic,
                "seatIndex" to seatIndex,
                "userId" to userId
            )
        )
    }

    suspend fun requestTakeMic(seatIndex: Int) {
        publish(
            mapOf(
                "event" to MatLiveEvents.requestTakeMic,
                "seatIndex" to seatIndex
            )
        )
    }

    suspend fun removeSpeaker(seatIndex: Int, userId: String) {
        publish(
            data = mapOf(
                "event" to MatLiveEvents.removeUserFromSeat,
                "seatIndex" to seatIndex,
                "userId" to userId
            )
        )
    }

}