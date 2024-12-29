package com.matnsolutions.matlive_sdk.audio.mangers

import com.matnsolutions.matlive_sdk.audio.define.MatLiveEvents
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.nio.charset.StandardCharsets

interface LiveRoomEventSenderManger {

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