package com.matnsolutions.matlive.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveRoomManger
import com.matnsolutions.matlive_sdk.audio.seats.MatLiveAudioRoomLayoutConfig
import com.matnsolutions.matlive_sdk.audio.seats.MatLiveAudioRoomLayoutRowConfig
import kotlinx.coroutines.launch

class AudioRoomViewModel : ViewModel() {
    private val matLiveRoomManger = MatLiveRoomManger.instance
    val messages = matLiveRoomManger.messages
    var loading = mutableStateOf(true)

    val seatService by matLiveRoomManger::seatService
    val onMic by matLiveRoomManger::onMic
    val currentUser by matLiveRoomManger::currentUser

    fun init(
        context: Context,
        roomId: String,
        appKey: String,
        userName: String,
        avatar: String,
        userId: String,
    ) {
        viewModelScope.launch {
            matLiveRoomManger.connect(
                appKey = appKey,
                context = context,
                roomId = roomId,
                name = userName,
                avatar = avatar,
                userId = userId,
                onInvitedToMic = {},
                onSendGift = {}
            )

            seatService.initWithConfig(
                MatLiveAudioRoomLayoutConfig(
                    rowSpacing = 16.0,
                    rowConfigs = listOf(
                        MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12),
                        MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12)
                    )
                )
            )
            loading.value = false
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            matLiveRoomManger.sendMessage(message)
        }
    }

    fun takeSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.takeSeat(setIndex)
        }
    }

    fun muteSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.muteSeat(setIndex)
        }
    }

    fun unMuteSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.unMuteSeat(setIndex)
        }
    }

    fun removeUserFromSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.removeUserFromSeat(setIndex)
        }
    }

    fun leaveSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.leaveSeat(setIndex)
        }
    }

    fun switchSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.switchSeat(setIndex)
        }
    }

    fun lockSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.lockSeat(setIndex)
        }
    }

    fun unLockSeat(setIndex: Int) {
        viewModelScope.launch {
            matLiveRoomManger.unLockSeat(setIndex)
        }
    }

    override fun onCleared() {
        super.onCleared()
        MatLiveRoomManger.instance.close()
    }
}
