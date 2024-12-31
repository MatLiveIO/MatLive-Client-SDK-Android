package com.matnsolutions.matlive_sdk.audio.mangers

import Utlis
import android.content.Context
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser
import com.matnsolutions.matlive_sdk.utils.kPrint
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.e2ee.BaseKeyProvider
import io.livekit.android.e2ee.E2EEOptions
import io.livekit.android.room.Room
import io.livekit.android.room.track.LocalAudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
//    private var _audioInputs: List<MediaDevice> = emptyList()
    private var _subscription: kotlinx.coroutines.Job? = null
    var currentUser: MatLiveUser? = null
    var audioTrack: LocalAudioTrack? = null
    var roomId: String = ""
//    private var _selectedAudioDevice: MediaDevice? = null
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
//        if (lkPlatformIs(PlatformType.android)) {
//            _checkPermissions()
//        }
//        if (lkPlatformIsMobile()) {
//            LiveKitClient.initialize(bypassVoiceProcessing = true)
//        }
//        _subscription = coroutineScope.launch {
//            Hardware.instance.onDeviceChange.collect {
//                _loadDevices(it)
//            }
//        }
//        _loadDevices(Hardware.instance.enumerateDevices())
    }

    private suspend fun _checkPermissions() {
//        val permissions = arrayOf(
//            Manifest.permission.BLUETOOTH,
//            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.RECORD_AUDIO
//        )
//
//        permissions.forEach { permission ->
//            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Handle permission request here, for now just print
//                kPrint("Permission $permission not granted")
//            }
//        }
    }

    suspend fun close() {
        _subscription?.cancel()
        _stopAudioStream()
        MatLiveRoomManger.instance.close()
        coroutineScope.cancel()
    }

//    private suspend fun _loadDevices(devices: List<MediaDevice>) {
//        _audioInputs = devices.filter { it.kind == "audioinput" }
//
//        if (_audioInputs.isNotEmpty()) {
//            if (_selectedAudioDevice == null) {
//                _selectedAudioDevice = _audioInputs.first()
//                _changeLocalAudioTrack()
//            }
//        }
//    }

    private suspend fun _stopAudioStream() {
        audioTrack?.stop()
        audioTrack = null
    }

    private suspend fun _changeLocalAudioTrack() {
//        audioTrack?.stop()
//        audioTrack = null
//
//        _selectedAudioDevice?.let {
//            audioTrack = LocalAudioTrack(
//                AudioCaptureOptions(
//                    deviceId = it.deviceId,
//                )
//            )
//        }
    }

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
//                    audioTrackPublishDefaults = AudioTrackPublishDefaults(
//                        name = "custom_audio_track_name",
//                        audioBitrate = AudioPresets.musicHighQualityStereo,
//                    ),
                    e2eeOptions = e2eeOptions,
                )
//                ,overrides = LiveKitOverrides(
//                    audioOptions = AudioOptions(audioProcessorOptions = audioProcessorOptions),
//                ),
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

            MatLiveRoomManger.instance.room = room
//            MatLiveRoomManger.instance.listener = room.createListener()
            MatLiveRoomManger.instance.setUp(
                onInvitedToMic,
                onSendGift,
            )
            delay(1000)
        } catch (error: Exception) {
            kPrint("Could not connect $error")
            throw Exception("Failed to update metadata: $error");
        }
    }
}