package com.matnsolutions.matlive_sdk.audio.mangers

import Utlis
import android.content.Context
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser
import com.matnsolutions.matlive_sdk.utils.kPrint
import io.livekit.android.AudioOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.audio.AudioProcessorOptions
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.e2ee.BaseKeyProvider
import io.livekit.android.e2ee.E2EEOptions
import io.livekit.android.room.participant.AudioTrackPublishDefaults
import io.livekit.android.room.track.LocalAudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

class MatLiveJoinRoomManger private constructor() {
    companion object {
        val instance: MatLiveJoinRoomManger by lazy { MatLiveJoinRoomManger() }
    }

    private val _request = JoinRequest()

    var currentUser: MatLiveUser? = null
    var audioTrack: LocalAudioTrack? = null
    var roomId: String = ""

    var onInvitedToMic: ((Int) -> Unit)? = null
    var onSendGift: ((String) -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun init(
        onInvitedToMic: ((Int) -> Unit)?,
        onSendGift: ((String) -> Unit)?,
    ) {
        _request.url = Utlis.url
        this.onInvitedToMic = onInvitedToMic
        this.onSendGift = onSendGift
//        initLocalAudioTrack();
    }


//    suspend fun initLocalAudioTrack() {
//        audioTrack = LocalAudioTrack(
//            AudioCaptureOptions(
//                noiseSuppression = true, echoCancellation = true
//            )
//        )
//    }

    suspend fun close() {
        _stopAudioStream()
        MatLiveRoomManger.instance.close()
    }


    private suspend fun _stopAudioStream() {
        audioTrack?.stop()
        audioTrack = null
    }


    //    private  lateinit var application: Application;
    suspend fun connect(
        context: Context,
        token: String,
        name: String,
        avatar: String,
        userId: String,
        roomId: String,
        metadata: String? = null,
    ) {
        this.roomId = roomId
//        this.application = application
        _request.token = token
        currentUser = MatLiveUser(
            name = name,
            avatar = avatar,
            userId = userId,
            roomId = userId,
            metadata = metadata,
        )
        try {
            var e2eeOptions: E2EEOptions? = null
            if (_request.e2ee && _request.e2eeKey != null) {
                val keyProvider = BaseKeyProvider()
                e2eeOptions = E2EEOptions(keyProvider = keyProvider)
                keyProvider.setKey(key = _request.e2eeKey, participantId = userId)
            }

            val room = LiveKit.create(
                appContext = context,
                options = RoomOptions(
                    adaptiveStream = _request.adaptiveStream,
                    dynacast = _request.dynacast,
                    e2eeOptions = e2eeOptions,
//                    audioTrackPublishDefaults = AudioTrackPublishDefaults(audioBitrate = 3200)
                ),
                overrides = LiveKitOverrides(
                    audioOptions = AudioOptions(audioProcessorOptions = AudioProcessorOptions()),
                ),
            )
            room.prepareConnection(_request.url, _request.token)

            coroutineScope.launch {
                room.connect(
                    _request.url,
                    _request.token,
//                options = ConnectOptions(
//                    microphone = TrackOption(track = audioTrack),
//                )
                )
            }
            // Start a foreground service to keep the call from being interrupted if the
            // app goes into the background.
//            val foregroundServiceIntent = Intent(application, ForegroundService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                application.startForegroundService(foregroundServiceIntent)
//            } else {
//                application.startService(foregroundServiceIntent)
//            }
            MatLiveRoomManger.instance.room = room
//            MatLiveRoomManger.instance.listener = room.createListener()
            MatLiveRoomManger.instance.setUp(
                onInvitedToMic,
                onSendGift,
            )
            delay(1000)
            val audioHandler = room.audioHandler as AudioSwitchHandler
            audioHandler.let {
                it.selectDevice(it.availableAudioDevices.last())
            }
        } catch (error: Exception) {
            kPrint("Could not connect $error")
            throw Exception("Failed to update metadata: $error");
        }
    }
}
