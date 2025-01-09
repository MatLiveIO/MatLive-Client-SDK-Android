package com.matnsolutions.matlive_sdk.audio.define

data class JoinRequest(
    var url: String = "",
    var token: String = "",
    val e2ee: Boolean = false,
    val e2eeKey: String? = null,
    val simulcast: Boolean = true,
    val adaptiveStream: Boolean = true,
    val dynacast: Boolean = true,
    val preferredCodec: String = "VP8",
    val enableBackupVideoCodec: Boolean = true,
)