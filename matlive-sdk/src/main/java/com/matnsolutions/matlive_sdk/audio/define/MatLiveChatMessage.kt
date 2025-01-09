package com.matnsolutions.matlive_sdk.audio.define


data class MatLiveChatMessage(
    val roomId: String? = null,
    val message: String,
    val user: MatLiveUser
)