package com.matnsolutions.matlive_sdk.audio.seats

import android.util.Log
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveRoomManger
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRoomAudioSeat
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser
import com.matnsolutions.matlive_sdk.services.MatLiveService
import com.matnsolutions.matlive_sdk.utils.kPrint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class RoomSeatService {
    var hostSeatIndex = 0
    private val _seatList = MutableStateFlow<List<MatLiveRoomAudioSeat>>(emptyList())
    val seatList: StateFlow<List<MatLiveRoomAudioSeat>> = _seatList.asStateFlow()
    var isBatchOperation = false

    private val _maxIndex: Int get() = _seatList.value.size - 1
    private val subscriptions = mutableListOf<Any>()

    private val roomId: String get() = MatLiveRoomManger.instance.roomId
    var layoutConfig: MatLiveAudioRoomLayoutConfig? = null
    private val matLiveService = MatLiveService()

    fun initWithConfig(config: MatLiveAudioRoomLayoutConfig) {
        layoutConfig = config
        _seatList.value = emptyList()
        initSeat(config)
        val metadata = MatLiveRoomManger.instance.room?.metadata
        seatsFromMetadata(metadata)
    }

    private fun initSeat(config: MatLiveAudioRoomLayoutConfig) {
        val newSeatList = mutableListOf<MatLiveRoomAudioSeat>()
        for (columIndex in config.rowConfigs.indices) {
            val rowConfig = config.rowConfigs[columIndex]
            for (rowIndex in 0 until rowConfig.count) {
                val seat = MatLiveRoomAudioSeat(
                    newSeatList.size,
                    rowIndex,
                    columIndex,
                )
                newSeatList.add(seat)
            }
        }
        _seatList.value = newSeatList
    }

    suspend fun addSeatRow(oldCount: Int, newCount: Int) {
        layoutConfig = MatLiveAudioRoomLayoutConfig(
            rowConfigs = List(newCount / 5) {
                MatLiveAudioRoomLayoutRowConfig()
            }
        )
        val columIndex = newCount / 5
        val newSeatList = _seatList.value.toMutableList()
        for (rowIndex in oldCount until newCount) {
            val seat = MatLiveRoomAudioSeat(newSeatList.size, rowIndex, columIndex)
            newSeatList.add(seat)
        }
        _seatList.value = newSeatList
        matLiveService.updateRoomMetadata(
            roomId = roomId,
            metadata = getSeatInfo(),
        )
    }

    suspend fun removeSeatRow(oldCount: Int, newCount: Int) {
        val empty = _seatList.value.filter { it.currentUser.value == null }
        if (empty.size >= oldCount - newCount) {
            layoutConfig = MatLiveAudioRoomLayoutConfig(
                rowConfigs = List(newCount / 5) {
                    MatLiveAudioRoomLayoutRowConfig()
                }
            )
            val newSeatList = _seatList.value.toMutableList()
            for (rowIndex in oldCount - 1 downTo newCount) {
                val seat = newSeatList.getOrNull(rowIndex)
                if (seat == null) continue
                if (seat.currentUser.value == null) {
                    newSeatList.removeAt(rowIndex)
                } else {
                    val index = newSeatList.indexOfFirst { it.currentUser.value == null }
                    if (index != -1) {
                        switchSeat(
                            rowIndex,
                            index,
                            newSeatList[rowIndex].currentUser.value!!.userId,
                        )
                        newSeatList.removeAt(rowIndex)
                    }
                }
            }
            _seatList.value = newSeatList
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun takeSeat(seatIndex: Int, user: MatLiveUser) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.seatIndex == seatIndex && seat.currentUser.value == null) {
            seat.currentUser.value = user
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun switchSeat(fromSeatIndex: Int, toSeatIndex: Int, userId: String) {
        kPrint(fromSeatIndex)
        kPrint(toSeatIndex)
        if (fromSeatIndex == -1 || fromSeatIndex > _maxIndex) return
        if (toSeatIndex == -1 || toSeatIndex > _maxIndex) return
        var tempUser: MatLiveUser?

        // Store user from fromSeat
        val fromSeat = _seatList.value[fromSeatIndex]
        val toSeat = _seatList.value[toSeatIndex]
        tempUser = fromSeat.currentUser.value

        if (toSeat.currentUser.value == null) {
            fromSeat.currentUser.value = null
        } else {
            tempUser = null
        }
        kPrint(tempUser)

        // Move user to toSeat
        if (tempUser != null) {
            toSeat.currentUser.value = tempUser
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun leaveSeat(seatIndex: Int, userId: String) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.currentUser.value != null &&
            seat.currentUser.value?.userId == userId
        ) {
            seat.currentUser.value = null
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun setMicOpened(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.currentUser.value == null) {
            seat.isLocked.value = false
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun unLockSeat(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.seatIndex == seatIndex && seat.currentUser.value == null) {
            seat.isLocked.value = false
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun lockSeat(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.seatIndex == seatIndex && seat.currentUser.value == null) {
            seat.isLocked.value = true
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun muteSeat(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.currentUser.value != null) {
            seat.currentUser.value!!.isMicOnNotifier.value = false
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun unMuteSeat(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.currentUser.value != null) {
            seat.currentUser.value!!.isMicOnNotifier.value = true
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun removeUserFromSeat(seatIndex: Int): String? {
        if (seatIndex == -1 || seatIndex > _maxIndex) return null
        val seat = _seatList.value[seatIndex]
        val userId = seat.currentUser.value?.userId
        if (seat.currentUser.value != null) {
            seat.currentUser.value = null
            matLiveService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
        return userId
    }

    private suspend fun _leaveSeatIfHave() {
         val seat = _seatList.value.firstOrNull {
            it.currentUser.value?.userId == MatLiveRoomManger.instance.currentUser?.userId
        }
        seat?.currentUser?.value = null
        matLiveService.updateRoomMetadata(
            roomId = roomId,
            metadata = getSeatInfo(),
        )
    }

    suspend fun clear() {
        _leaveSeatIfHave()
        _seatList.value = emptyList()
        isBatchOperation = false
        subscriptions.forEach {
            //TODO: add cancel logic
        }
        subscriptions.clear()
    }

    private fun getSeatInfo(): String {
        val seats = _seatList.value.map { seat ->
            mapOf(
                "seatIndex" to seat.seatIndex,
                "rowIndex" to seat.rowIndex,
                "columnIndex" to seat.columnIndex,
                "isLocked" to seat.isLocked.value,
                "currentUser" to seat.currentUser.value?.let { user ->
                    mapOf(
                        "userId" to user.userId,
                        "name" to user.name,
                        "avatar" to user.avatar,
                        "roomId" to user.roomId,
                        "isMuted" to user.isMicOnNotifier.value,
                    )
                }
            )
        }
        val jsonString =  JSONObject(mapOf("seats" to seats)).toString()
        return jsonString
    }

    fun seatsFromMetadata(metadata: String?) {
        if (metadata == null || !metadata.contains("seats")) return

        try {
            val jsonObject = JSONObject(metadata)
            val seatsArray = jsonObject.getJSONArray("seats") ?: return

            for (i in 0 until seatsArray.length()) {
                val item = seatsArray.getJSONObject(i)
                val seatIndex = item.optInt("seatIndex", -1)
                if (seatIndex == -1) continue

                val seat = _seatList.value.getOrNull(seatIndex) ?: continue
                seat.isLocked.value = item.optBoolean("isLocked", false)

                if (item.has("currentUser") && !item.isNull("currentUser")) {
                    val currentUser = item.getJSONObject("currentUser")

                    if (seat.currentUser.value != null) {
                        seat.currentUser.value!!.apply {
                            name = currentUser.getString("name")
                            userId = currentUser.getString("userId")
                            roomId = currentUser.getString("roomId")
                            isMicOnNotifier.value = currentUser.optBoolean("isMuted", false)
                            avatarUrlNotifier.value = currentUser.optString("avatar")
                        }
                    } else {
                        seat.currentUser.value = MatLiveUser(
                            roomId = currentUser.getString("roomId"),
                            name = currentUser.getString("name"),
                            userId = currentUser.getString("userId"),
                            avatar = currentUser.optString("avatar")
                        ).apply {
                            isMicOnNotifier.value = currentUser.optBoolean("isMuted", false)
                            avatarUrlNotifier.value = currentUser.optString("avatar")
                        }
                    }
                } else {
                    seat.currentUser.value = null
                }
            }
        } catch (e: org.json.JSONException) {
            // Handle JSON parsing error
            kPrint("seatsFromMetadata error: $e")
            Log.e("seatsFromMetadata", "JSON parsing error: ${e.message}")
            return
        } catch (e: Exception) {
            // Handle other exceptions
            kPrint("seatsFromMetadata error: $e")
            Log.e("seatsFromMetadata", "Unexpected error: ${e.message}")
            return
        }
    }
}
