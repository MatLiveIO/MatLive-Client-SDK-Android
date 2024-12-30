//package com.matnsolutions.matlive_sdk.audio.mangers
//
//import com.matnsolutions.matlive_sdk.audio.define.MatLiveEvents
//import org.json.JSONObject
//
//import java.nio.charset.StandardCharsets
//
//interface LiveRoomEventSenderManger {
//
//    private suspend fun publish(data: Map<String, Any>) {
//        val room = MatLiveRoomManger.instance.room
//        if (room != null) {
//            val jsonObject = JSONObject(data)
//
//            // Add user data
//            val userObject = JSONObject().apply {
//                put("userId", MatLiveJoinRoomManger.instance.currentUser?.userId)
//                put("name", MatLiveJoinRoomManger.instance.currentUser?.name)
//                put("avatar", MatLiveJoinRoomManger.instance.currentUser?.avatar)
//            }
//            jsonObject.put("user", userObject)
//
//            // Add room ID
//            jsonObject.put("roomId", room.name.toString())
//
//            // Convert to byte array and publish
//            val decoded = jsonObject.toString().toByteArray(StandardCharsets.UTF_8)
//            room.localParticipant.publishData(decoded)
//        }
//    }
//
//     suspend fun sendMessage(message: String) {
//        publish(
//            mapOf(
//                "event" to MatLiveEvents.sendMessage,
//                "message" to message
//            )
//        )
//    }
//
//    suspend fun sendGift(gift: String) {
//        publish(
//            mapOf(
//                "event" to MatLiveEvents.sendGift,
//                "gift" to gift
//            )
//        )
//    }
//
//    suspend fun clearChat() {
//        publish(
//            mapOf(
//                "event" to MatLiveEvents.clearChat
//            )
//        )
//    }
//
//    suspend fun inviteUserToTakeMic(userId: String, seatIndex: Int) {
//        publish(
//            mapOf(
//                "event" to MatLiveEvents.inviteUserToTakeMic,
//                "seatIndex" to seatIndex,
//                "userId" to userId
//            )
//        )
//    }
//
//    suspend fun requestTakeMic(seatIndex: Int) {
//        publish(
//            mapOf(
//                "event" to MatLiveEvents.requestTakeMic,
//                "seatIndex" to seatIndex
//            )
//        )
//    }
//}