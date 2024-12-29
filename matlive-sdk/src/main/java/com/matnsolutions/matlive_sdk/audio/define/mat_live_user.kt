package com.matnsolutions.matlive_sdk.audio.define

import androidx.lifecycle.MutableLiveData

data class MatLiveUser(
    var userId: String,
    var name: String,
    val avatar: String,
    var roomId: String,
    val metadata: String? = null
) {
    var streamID: String? = null
    var viewID: Int = -1
    var videoViewNotifier: MutableLiveData<Any?> = MutableLiveData(null)
    var isCameraOnNotifier: MutableLiveData<Boolean> = MutableLiveData(false)
    var isMicOnNotifier: MutableLiveData<Boolean> = MutableLiveData(true)
    var avatarUrlNotifier: MutableLiveData<String?> = MutableLiveData(avatar)
}

enum class MatLiveUserRole {
    audience,
    coHost,
    host
}