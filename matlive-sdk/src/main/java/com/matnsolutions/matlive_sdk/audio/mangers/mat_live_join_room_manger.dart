import 'dart:async';

import 'package:livekit_client/livekit_client.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_user.dart';
import 'package:matlive_client_sdk_flutter/audio/mangers/mat_live_room_manger.dart';
import 'package:matlive_client_sdk_flutter/services/utlis.dart';
import 'package:matlive_client_sdk_flutter/utils/kprint.dart';
import 'package:permission_handler/permission_handler.dart';

class JoinRequest {
  JoinRequest({
    this.url = "",
    this.token = "",
    this.e2ee = false,
    this.e2eeKey,
    this.simulcast = true,
    this.adaptiveStream = true,
    this.dynacast = true,
    this.preferredCodec = 'VP8',
    this.enableBackupVideoCodec = true,
  });

  String url;
  String token;
  final bool e2ee;
  final String? e2eeKey;
  final bool simulcast;
  final bool adaptiveStream;
  final bool dynacast;
  final String preferredCodec;
  final bool enableBackupVideoCodec;
}

class MatLiveJoinRoomManger {
  static final MatLiveJoinRoomManger instance =
      MatLiveJoinRoomManger._internal();

  MatLiveJoinRoomManger._internal();

  final JoinRequest _request = JoinRequest();
  List<MediaDevice> _audioInputs = [];
  StreamSubscription? _subscription;
  MatLiveUser? currentUser;
  LocalAudioTrack? audioTrack;
  String roomId = "";
  MediaDevice? _selectedAudioDevice;
  Function(int seatIndex)? onInvitedToMic;
  Function(String data)? onSendGift;

  Future<void> init({
    required Function(int seatIndex)? onInvitedToMic,
    required Function(String data)? onSendGift,
  }) async {
    _request.url = Utlis.url;
    this.onInvitedToMic = onInvitedToMic;
    this.onSendGift = onSendGift;
    if (lkPlatformIs(PlatformType.android)) {
      await _checkPermissions();
    }
    if (lkPlatformIsMobile()) {
      await LiveKitClient.initialize(bypassVoiceProcessing: true);
    }
    _subscription =
        Hardware.instance.onDeviceChange.stream.listen(_loadDevices);
    await Hardware.instance.enumerateDevices().then(_loadDevices);
  }

  Future<void> _checkPermissions() async {
    final res = await Future.wait([
      Permission.bluetooth.request(),
      Permission.bluetoothConnect.request(),
      Permission.microphone.request(),
    ]);
    kPrint(res);
  }

  Future<void> close() async {
    _subscription?.cancel();
    await _stopAudioStream();
    await MatLiveRoomManger.instance.close();
  }

  Future<void> _loadDevices(List<MediaDevice> devices) async {
    _audioInputs = devices.where((d) => d.kind == 'audioinput').toList();

    if (_audioInputs.isNotEmpty) {
      if (_selectedAudioDevice == null) {
        _selectedAudioDevice = _audioInputs.first;
        await _changeLocalAudioTrack();
      }
    }
  }

  Future<void> _stopAudioStream() async {
    await audioTrack?.stop();
    audioTrack = null;
  }

  Future<void> _changeLocalAudioTrack() async {
    if (audioTrack != null) {
      await audioTrack!.stop();
      audioTrack = null;
    }

    if (_selectedAudioDevice != null) {
      audioTrack = await LocalAudioTrack.create(AudioCaptureOptions(
        deviceId: _selectedAudioDevice!.deviceId,
      ));
    }
  }

  Future<void> connect({
    required String token,
    required String name,
    required String avatar,
    required String userId,
    required String roomId,
    String? metadata,
  }) async {
    this.roomId = roomId;
    _request.token = token;
    currentUser = MatLiveUser(
      name: name,
      avatar: avatar,
      userId: userId,
      roomId: userId,
      metadata: metadata,
    );
    try {
      E2EEOptions? e2eeOptions;
      if (_request.e2ee && _request.e2eeKey != null) {
        final keyProvider = await BaseKeyProvider.create();
        e2eeOptions = E2EEOptions(keyProvider: keyProvider);
        await keyProvider.setKey(_request.e2eeKey!);
      }

      final room = Room(
        roomOptions: RoomOptions(
          adaptiveStream: _request.adaptiveStream,
          dynacast: _request.dynacast,
          defaultAudioPublishOptions: const AudioPublishOptions(
            name: 'custom_audio_track_name',
            audioBitrate: AudioPreset.musicHighQualityStereo,
          ),
          e2eeOptions: e2eeOptions,
        ),
      );
      await room.prepareConnection(_request.url, _request.token);
      await room.connect(
        _request.url,
        _request.token,
        fastConnectOptions: FastConnectOptions(
          microphone: TrackOption(track: audioTrack),
        ),
      );
      MatLiveRoomManger.instance.room = room;
      MatLiveRoomManger.instance.listener = room.createListener();
      MatLiveRoomManger.instance.setUp(
        onInvitedToMic,
        onSendGift,
      );
    } catch (error) {
      kPrint('Could not connect $error');
    }
  }
}
