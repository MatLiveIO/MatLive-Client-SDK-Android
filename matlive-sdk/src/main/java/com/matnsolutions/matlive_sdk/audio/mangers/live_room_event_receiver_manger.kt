package com.matnsolutions.matlive_sdk.audio.mangers

import com.matnsolutions.matlive_sdk.audio.define.MatLiveRequestTakeMic
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.define.MatLiveEvents
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser
import com.matnsolutions.matlive_sdk.audio.seats.RoomSeatService
import com.matnsolutions.matlive_sdk.utils.kPrint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

open class LiveRoomEventReceiverManger {
    open var seatService: RoomSeatService? = null
    private val _messages = MutableStateFlow<List<MatLiveChatMessage>>(emptyList())
    open val messages: StateFlow<List<MatLiveChatMessage>> = _messages.asStateFlow()
    private val _inviteRequests = MutableStateFlow<List<MatLiveRequestTakeMic>>(emptyList())
    open val inviteRequests: StateFlow<List<MatLiveRequestTakeMic>> = _inviteRequests.asStateFlow()


    fun receivedData(
        data: Map<String, Any>,
        onInvitedToMic: ((Int) -> Unit)? = null,
        onSendGift: ((String) -> Unit)? = null
    ) {
        if (seatService == null) return
        try {
            val event = data["event"] as Int
            val user = data["user"] as Map<*, *>
            val matUser = MatLiveUser(
                userId = user["userId"] as String,
                name = user["name"] as String,
                avatar = user["avatar"] as String,
                roomId = data["roomId"] as String,
                metadata = data["metadata"] as String?
            )
            when (event) {
                MatLiveEvents.sendMessage -> {
                    _messages.value += MatLiveChatMessage(
                        user = matUser,
                        message = data["message"] as String,
                        roomId = data["roomId"] as String
                    )
                }
                MatLiveEvents.clearChat -> {
                    _messages.value = emptyList()
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
        }
    }


    private suspend fun _publish(data: Map<String, Any>) {
        val room = MatLiveRoomManger.instance.room
        if (room != null) {
            val mutableData = data.toMutableMap()
            mutableData["user"] = mapOf(
                "userId" to MatLiveJoinRoomManger.instance.currentUser?.userId,
                "name" to MatLiveJoinRoomManger.instance.currentUser?.name,
                "avatar" to MatLiveJoinRoomManger.instance.currentUser?.avatar
            )
            mutableData["roomId"] = room.name.toString()
            val decoded = Json.encodeToString(mutableData).toByteArray(StandardCharsets.UTF_8)
            room.localParticipant.publishData(decoded)
        }
    }

    open suspend fun sendMessage(message: String) {
        _publish(
            mapOf(
                "event" to MatLiveEvents.sendMessage,
                "message" to message
            )
        )
    }

    suspend fun sendGift(gift: String) {
        _publish(
            mapOf(
                "event" to MatLiveEvents.sendGift,
                "gift" to gift
            )
        )
    }

    suspend fun clearChat() {
        _publish(
            mapOf(
                "event" to MatLiveEvents.clearChat
            )
        )
    }

    suspend fun inviteUserToTakeMic(userId: String, seatIndex: Int) {
        _publish(
            mapOf(
                "event" to MatLiveEvents.inviteUserToTakeMic,
                "seatIndex" to seatIndex,
                "userId" to userId
            )
        )
    }

    suspend fun requestTakeMic(seatIndex: Int) {
        _publish(
            mapOf(
                "event" to MatLiveEvents.requestTakeMic,
                "seatIndex" to seatIndex
            )
        )
    }
}