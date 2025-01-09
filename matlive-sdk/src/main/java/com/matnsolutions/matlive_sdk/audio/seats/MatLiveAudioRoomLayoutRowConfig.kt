package com.matnsolutions.matlive_sdk.audio.seats

data class MatLiveAudioRoomLayoutRowConfig(
    var count: Int = 5,
    var seatSpacing: Int = 0
) {
    override fun toString(): String {
        return "row config:{count:$count, spacing:$seatSpacing}"
    }
}