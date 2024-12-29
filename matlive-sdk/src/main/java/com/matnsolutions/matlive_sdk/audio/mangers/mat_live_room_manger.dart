// ignore_for_file: overridden_fields

import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:livekit_client/livekit_client.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_chat_message.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_request_take_mic.kt';
import 'package:matlive_client_sdk_flutter/audio/mangers/live_room_event_sender_manger.dart';
import 'package:matlive_client_sdk_flutter/audio/mangers/live_room_event_receiver_manger.dart';
import 'package:matlive_client_sdk_flutter/audio/mangers/mat_live_join_room_manger.dart';
import 'package:matlive_client_sdk_flutter/audio/seats/room_seat_service.dart';
import 'package:matlive_client_sdk_flutter/replay_kit_channel.dart';
import 'package:matlive_client_sdk_flutter/utils/kprint.dart';

enum ParticipantTrackType {
  kUserMedia,
  kScreenShare,
}

class ParticipantTrack {
  ParticipantTrack({
    required this.participant,
    this.type = ParticipantTrackType.kUserMedia,
  });

  Participant participant;
  final ParticipantTrackType type;
}

class MatLiveRoomManger extends LiveRoomEventSenderManger
    with LiveRoomEventReceiverManger {
  static final MatLiveRoomManger instance = MatLiveRoomManger._internal();

  MatLiveRoomManger._internal();

  Room? room;
  EventsListener<RoomEvent>? listener;
  List<ParticipantTrack> participantTracks = [];
  @override
  late ValueNotifier<List<MatLiveChatMessage>> messages;
  @override
  late ValueNotifier<List<MatLiveRequestTakeMic>> inviteRequests;
  @override
  RoomSeatService? seatService;

  EventsListener<RoomEvent>? get _listener => listener;

  bool get fastConnection => room?.engine.fastConnectOptions != null;
  bool _flagStartedReplayKit = false;
  bool _isSetUpped = false;

  void setUp(
    Function(int seatIndex)? onInvitedToMic,
    Function(String data)? onSendGift,
  ) {
    if (room == null && _isSetUpped) return;
    seatService = RoomSeatService();
    messages = ValueNotifier<List<MatLiveChatMessage>>([]);
    inviteRequests = ValueNotifier<List<MatLiveRequestTakeMic>>([]);
    _askPublish(false);
    // add callback for a `RoomEvent` as opposed to a `ParticipantEvent`
    room?.addListener(_onRoomDidUpdate);
    // add callbacks for finer grained events
    _setUpListeners(
      onAudioPlaybackStatusChanged: (event) {
        kPrint(event);
      },
      onDataReceivedEvent: (data) {
        kPrint(data);
        if (data != null) {
          receivedData(
            data,
            onInvitedToMic,
            onSendGift,
          );
        }
      },
      onDisconnect: () {},
      onRoomRecordingStatusChanged: (event) {
        kPrint(event);
      },
    );
    _sortParticipants();

    if (lkPlatformIs(PlatformType.android)) {
      Hardware.instance.setSpeakerphoneOn(true);
    }

    if (lkPlatformIs(PlatformType.iOS) && room != null) {
      ReplayKitChannel.listenMethodChannel(room!);
    }
    _isSetUpped = true;
  }

  Future<void> close() async {
    if (lkPlatformIs(PlatformType.iOS)) {
      ReplayKitChannel.closeReplayKit();
    }
    _isSetUpped = false;
    seatService?.clear();
    messages.dispose();
    inviteRequests.dispose();
    room?.removeListener(_onRoomDidUpdate);

    await _listener?.dispose();
    await room?.dispose();
  }

  Future<void> takeSeat(int seatIndex) async {
    await _askPublish(true);
    await seatService?.takeSeat(
      seatIndex,
      MatLiveJoinRoomManger.instance.currentUser!,
    );
  }

  Future<void> lockSeat(int seatIndex) async {
    await seatService?.lockSeat(seatIndex);
  }

  Future<void> unLockSeat(int seatIndex) async {
    await seatService?.unLockSeat(seatIndex);
  }

  Future<void> leaveSeat(int seatIndex) async {
    await _askPublish(false);
    await seatService?.leaveSeat(
      seatIndex,
      MatLiveJoinRoomManger.instance.currentUser!.userId,
    );
  }

  @override
  Future<void> sendMessage(String message) async {
    messages.value = messages.value
      ..add(MatLiveChatMessage(
        roomId: MatLiveJoinRoomManger.instance.roomId,
        user: MatLiveJoinRoomManger.instance.currentUser!,
        message: message,
      ));
    await super.sendMessage(message);
  }

  Future<void> muteSeat(int seatIndex) async {
    await _askPublishMute(true);
    await seatService?.muteSeat(seatIndex);
  }

  Future<void> unMuteSeat(int seatIndex) async {
    kPrint('unMuteSeat');
    await _askPublishMute(false);
    await seatService?.unMuteSeat(seatIndex);
  }

  Future<void> removeUserFromSeat(int seatIndex) async {
    kPrint('removeUserFromSeat');
    await seatService?.removeUserFromSeat(seatIndex);
  }

  Future<void> switchSeat(
    int toSeatIndex,
  ) async {
    final userId = MatLiveJoinRoomManger.instance.currentUser!.userId;
    final seatId = seatService?.seatList.value
        .indexWhere((item) => item.currentUser.value?.userId == userId);
    if (seatId == null || seatId == -1) return;
    await seatService?.switchSeat(
      seatId,
      toSeatIndex,
      userId,
    );
    kPrint('switchSeat');
  }

  void _setUpListeners({
    required Function onDisconnect,
    required Function(RoomRecordingStatusChanged event)
        onRoomRecordingStatusChanged,
    required Function(Map<String, dynamic>? data) onDataReceivedEvent,
    required Function(AudioPlaybackStatusChanged event)
        onAudioPlaybackStatusChanged,
  }) {
    if (_listener != null) {
      _listener!
        ..on<ParticipantEvent>((event) => _sortParticipants())
        ..on<LocalTrackPublishedEvent>((_) => _sortParticipants())
        ..on<LocalTrackUnpublishedEvent>((_) => _sortParticipants())
        ..on<TrackSubscribedEvent>((_) => _sortParticipants())
        ..on<TrackUnsubscribedEvent>((_) => _sortParticipants())
        ..on<TrackE2EEStateEvent>(_onE2EEStateEvent)
        ..on<RoomAttemptReconnectEvent>((event) {
          kPrint('RoomAttemptReconnectEvent ${event.nextRetryDelaysInMs}ms');
        })
        ..on<LocalTrackSubscribedEvent>((event) {
          kPrint('LocalTrackSubscribedEvent: ${event.trackSid}');
        })
        ..on<ParticipantNameUpdatedEvent>((event) {
          kPrint('ParticipantNameUpdatedEvent');
          _sortParticipants();
        })
        ..on<ParticipantMetadataUpdatedEvent>((event) {
          kPrint('ParticipantMetadataUpdatedEvent');
        })
        ..on<RoomMetadataChangedEvent>((event) {
          kPrint('RoomMetadataChangedEvent ${event.metadata}');
          if (event.metadata != null &&
              event.metadata!.isNotEmpty &&
              event.metadata!.contains('seats')) {
            seatService!.seatsFromMetadata(event.metadata);
          }
        })
        ..on<AudioPlaybackStatusChanged>((event) async {
          if (!room!.canPlaybackAudio) {
            kPrint('AudioPlaybackStatusChanged Audio playback failed');
            await room!.startAudio();
          }
          onAudioPlaybackStatusChanged(event);
        })
        ..on<RoomDisconnectedEvent>((event) async {
          if (event.reason != null) {
            kPrint('RoomDisconnectedEvent ${event.reason}');
          }
          onDisconnect();
        })
        ..on<RoomRecordingStatusChanged>((event) {
          onRoomRecordingStatusChanged(event);
        })
        ..on<DataReceivedEvent>((event) {
          onDataReceivedEvent(jsonDecode(utf8.decode(event.data)));
        });
    }
  }

  Future<void> _askPublishMute(bool value) async {
    if (value) {
      await room?.localParticipant?.setMicrophoneEnabled(true);
    } else {
      await room?.localParticipant?.setMicrophoneEnabled(false);
    }
  }

  Future<void> _askPublish(bool value) async {
    try {
      if (value) {
        MatLiveJoinRoomManger.instance.audioTrack?.start();
      } else {
        MatLiveJoinRoomManger.instance.audioTrack?.stop();
      }
    } catch (error) {
      kPrint('could not publish audio: $error');
    }
    await room?.localParticipant?.setMicrophoneEnabled(value);
    await room?.localParticipant?.setCameraEnabled(false);
  }

  void _onRoomDidUpdate() {
    _sortParticipants();
  }

  void _onE2EEStateEvent(TrackE2EEStateEvent e2eeState) {
    kPrint('e2ee state: $e2eeState');
  }

  void _sortParticipants() {
    List<ParticipantTrack> userMediaTracks = [];
    if (room != null) {
      for (var participant in room!.remoteParticipants.values) {
        for (var t in participant.videoTrackPublications) {
          if (!t.isScreenShare) {
            userMediaTracks.add(ParticipantTrack(participant: participant));
          }
        }
      }
      // sort speakers for the grid
      userMediaTracks.sort((a, b) {
        // loudest speaker first
        if (a.participant.isSpeaking && b.participant.isSpeaking) {
          if (a.participant.audioLevel > b.participant.audioLevel) {
            return -1;
          } else {
            return 1;
          }
        }

        // last spoken at
        final aSpokeAt = a.participant.lastSpokeAt?.millisecondsSinceEpoch ?? 0;
        final bSpokeAt = b.participant.lastSpokeAt?.millisecondsSinceEpoch ?? 0;

        if (aSpokeAt != bSpokeAt) {
          return aSpokeAt > bSpokeAt ? -1 : 1;
        }

        // video on
        if (a.participant.hasVideo != b.participant.hasVideo) {
          return a.participant.hasVideo ? -1 : 1;
        }

        // joinedAt
        return a.participant.joinedAt.millisecondsSinceEpoch -
            b.participant.joinedAt.millisecondsSinceEpoch;
      });

      final localParticipantTracks =
          room!.localParticipant?.videoTrackPublications;
      if (localParticipantTracks != null) {
        for (var t in localParticipantTracks) {
          if (t.isScreenShare) {
            if (lkPlatformIs(PlatformType.iOS)) {
              if (!_flagStartedReplayKit) {
                _flagStartedReplayKit = true;

                ReplayKitChannel.startReplayKit();
              }
            }
          } else {
            if (lkPlatformIs(PlatformType.iOS)) {
              if (_flagStartedReplayKit) {
                _flagStartedReplayKit = false;

                ReplayKitChannel.closeReplayKit();
              }
            }

            userMediaTracks
                .add(ParticipantTrack(participant: room!.localParticipant!));
          }
        }
      }
      participantTracks = userMediaTracks;
    }
  }
}
