package com.matnsolutions.matlive_sdk.audio.seats

import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveJoinRoomManger
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveRoomManger
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRoomAudioSeat
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser
import com.matnsolutions.matlive_sdk.services.LiveKitService
import com.matnsolutions.matlive_sdk.utils.kPrint
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class RoomSeatService {
    var hostSeatIndex = 0
    private val _seatList = MutableStateFlow<List<MatLiveRoomAudioSeat>>(emptyList())
    val seatList: StateFlow<List<MatLiveRoomAudioSeat>> = _seatList.asStateFlow()
    var isBatchOperation = false

    private val _maxIndex: Int get() = _seatList.value.size - 1
    private val subscriptions = mutableListOf<Any>()

    private val roomId: String get() = MatLiveJoinRoomManger.instance.roomId
    var layoutConfig: MatLiveAudioRoomLayoutConfig? = null
    private val _liveKitService = LiveKitService(baseUrl = "https://tkstg.t-wasel.com")

    fun initWithConfig(config: MatLiveAudioRoomLayoutConfig) {
        layoutConfig = config
        _seatList.value = emptyList()
        initSeat(config)
        seatsFromMetadata(MatLiveRoomManger.instance.room?.metadata)
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
        _liveKitService.updateRoomMetadata(
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
            _liveKitService.updateRoomMetadata(
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
            _liveKitService.updateRoomMetadata(
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
            _liveKitService.updateRoomMetadata(
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
            _liveKitService.updateRoomMetadata(
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
            _liveKitService.updateRoomMetadata(
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
            _liveKitService.updateRoomMetadata(
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
            _liveKitService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun muteSeat(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.currentUser.value != null) {
            seat.currentUser.value!!.isMicOnNotifier.value = true
            _liveKitService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun unMuteSeat(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.currentUser.value != null) {
            seat.currentUser.value!!.isMicOnNotifier.value = false
            _liveKitService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    suspend fun removeUserFromSeat(seatIndex: Int) {
        if (seatIndex == -1 || seatIndex > _maxIndex) return
        val seat = _seatList.value[seatIndex]
        if (seat.currentUser.value != null) {
            seat.currentUser.value = null
            _liveKitService.updateRoomMetadata(
                roomId = roomId,
                metadata = getSeatInfo(),
            )
        }
    }

    private suspend fun _leaveSeatIfHave() {
         val seat = _seatList.value.firstOrNull {
            it.currentUser.value?.userId == MatLiveJoinRoomManger.instance.currentUser?.userId
        }
        seat?.currentUser?.value = null
        _liveKitService.updateRoomMetadata(
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
        return Json.encodeToString(mapOf("seats" to seats))
    }

    fun seatsFromMetadata(metadata: String?) {
        if (metadata == null || !metadata.contains("seats")) return
        val seats = Json.decodeFromString<Map<String, List<Map<String, Any?>>>>(metadata)["seats"] ?: return
        for (item in seats) {
            val seat = _seatList.value.getOrNull(item["seatIndex"] as? Int ?: continue) ?: continue
            seat.isLocked.value = item["isLocked"] as? Boolean ?: false
            val currentUser = item["currentUser"] as? Map<String, Any?>
            if (seat.currentUser.value != null && currentUser != null) {
                seat.currentUser.value!!.apply {
                    name = currentUser["name"] as String
                    userId = currentUser["userId"] as String
                    roomId = currentUser["roomId"] as String
                    isMicOnNotifier.value = currentUser["isMuted"] as? Boolean ?: false
                    avatarUrlNotifier.value = currentUser["avatar"] as? String
                }
            } else if (currentUser != null) {
                seat.currentUser.value = MatLiveUser(
                    roomId = currentUser["roomId"] as String,
                    name = currentUser["name"] as String,
                    userId = currentUser["userId"] as String,
                    avatar = currentUser["avatar"] as String
                ).apply {
                    isMicOnNotifier.value = currentUser["isMuted"] as? Boolean ?: false
                    avatarUrlNotifier.value = currentUser["avatar"] as? String
                }
            } else if (currentUser == null) {
                seat.currentUser.value = null
            }
        }
    }
}