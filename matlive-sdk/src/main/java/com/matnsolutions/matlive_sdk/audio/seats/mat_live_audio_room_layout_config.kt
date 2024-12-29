package com.matnsolutions.matlive_sdk.audio.seats

data class MatLiveAudioRoomLayoutConfig(
    val rowSpacing: Double = 0.0,
    val rowConfigs: List<MatLiveAudioRoomLayoutRowConfig> = listOf(
        MatLiveAudioRoomLayoutRowConfig(),
        MatLiveAudioRoomLayoutRowConfig()
    )
) {
    override fun toString(): String {
        return "rowSpacing:$rowSpacing, rowConfigs:${rowConfigs.toString()}"
    }
}

data class MatLiveAudioRoomLayoutRowConfig(
    var count: Int = 5,
    var seatSpacing: Int = 0
) {
    override fun toString(): String {
        return "row config:{count:$count, spacing:$seatSpacing}"
    }
}