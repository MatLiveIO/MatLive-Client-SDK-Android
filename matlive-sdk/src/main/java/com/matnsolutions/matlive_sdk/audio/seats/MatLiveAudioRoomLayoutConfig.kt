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
