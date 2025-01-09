package com.matnsolutions.matlive_sdk.audio.define

import androidx.lifecycle.MutableLiveData

class MatLiveRoomAudioSeat(
    var seatIndex: Int,
    var rowIndex: Int,
    var columnIndex: Int
) {
    var seatKey: Any = Any() // Placeholder for GlobalKey equivalent
    var lastUser: MutableLiveData<MatLiveUser?> = MutableLiveData(null)
    var currentUser: MutableLiveData<MatLiveUser?> = MutableLiveData(null)
    var isLocked: MutableLiveData<Boolean> = MutableLiveData(false)
}