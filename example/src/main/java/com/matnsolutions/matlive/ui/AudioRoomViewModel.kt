package com.matnsolutions.matlive.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveJoinRoomManger
import com.matnsolutions.matlive_sdk.audio.seats.MatLiveAudioRoomLayoutConfig
import com.matnsolutions.matlive_sdk.audio.seats.MatLiveAudioRoomLayoutRowConfig
import kotlinx.coroutines.launch

class AudioRoomViewModel : ViewModel() {
    val matLiveRoomManger = MatLiveJoinRoomManger.instance
    val messages = matLiveRoomManger.messages
    var loading = mutableStateOf(true)

    fun init(
        context: Context,
        roomId: String,
        userName: String,
        avatar: String,
        userId: String,
    ) {
        viewModelScope.launch {
            matLiveRoomManger.init(
                onInvitedToMic = {},
                onSendGift = {}
            )
            matLiveRoomManger.connect(
                appKey  = "\$2b\$10\$e6xwXI/OuJBS8XSMT2V.ROye2ideAywvCdLtjBSvmKttwd0DwkInW",
                context = context,
                roomId = roomId,
                name = userName,
                avatar = avatar,
                userId = userId,
            )

            val seatService = matLiveRoomManger.seatService
            seatService?.initWithConfig(
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
        Log.e("onCleared", "DisposableEffect")
        MatLiveJoinRoomManger.instance.close()
//        viewModelScope.launch {
//            MatLiveJoinRoomManger.instance.close()
//        }
    }
}
