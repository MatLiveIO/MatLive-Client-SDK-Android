import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/audio/mangers/mat_live_join_room_manger.dart';
import 'package:matlive_client_sdk_flutter/audio/mangers/mat_live_room_manger.dart';
import 'package:matlive_client_sdk_flutter/audio/seats/mat_live_audio_room_layout_config.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_room_audio_seat.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_user.dart';
import 'package:matlive_client_sdk_flutter/services/livekit_service.dart';
import 'package:matlive_client_sdk_flutter/utils/kprint.dart';

class RoomSeatService {
  int hostSeatIndex = 0;
  late ValueNotifier<List<MatLiveRoomAudioSeat>> seatList;
  bool isBatchOperation = false;

  int get _maxIndex => seatList.value.length - 1;
  List<StreamSubscription<dynamic>> subscriptions = [];

  String get roomId => MatLiveJoinRoomManger.instance.roomId;
  MatLiveAudioRoomLayoutConfig? layoutConfig;
  final _liveKitService = LiveKitService(baseUrl: "https://tkstg.t-wasel.com");

  void initWithConfig(MatLiveAudioRoomLayoutConfig config) {
    layoutConfig = config;
    seatList = ValueNotifier<List<MatLiveRoomAudioSeat>>([]);
    initSeat(config);
    seatsFromMetadata(MatLiveRoomManger.instance.room!.metadata);
  }

  void initSeat(MatLiveAudioRoomLayoutConfig config) {
    for (var columIndex = 0;
        columIndex < config.rowConfigs.length;
        columIndex++) {
      final rowConfig = config.rowConfigs[columIndex];
      for (var rowIndex = 0; rowIndex < rowConfig.count; rowIndex++) {
        final seat = MatLiveRoomAudioSeat(
          seatList.value.length,
          rowIndex,
          columIndex,
        );
        seatList.value = [...seatList.value, seat];
      }
    }
  }

  Future<void> addSeatRow(int oldCount, int newCount) async {
    layoutConfig = MatLiveAudioRoomLayoutConfig(
      rowConfigs: List.generate(newCount ~/ 5, (index) {
        return MatLiveAudioRoomLayoutRowConfig();
      }),
    );
    final columIndex = newCount ~/ 5;
    for (var rowIndex = oldCount; rowIndex < newCount; rowIndex++) {
      final seat =
          MatLiveRoomAudioSeat(seatList.value.length, rowIndex, columIndex);
      seatList.value.add(seat);
    }
    await _liveKitService.updateRoomMetadata(
      roomId: roomId,
      metadata: getSeatInfo(),
    );
  }

  Future<void> removeSeatRow(int oldCount, int newCount) async {
    final empty =
        seatList.value.where((item) => item.currentUser.value == null);
    if (empty.length >= oldCount - newCount) {
      layoutConfig = MatLiveAudioRoomLayoutConfig(
        rowConfigs: List.generate(newCount ~/ 5, (index) {
          return MatLiveAudioRoomLayoutRowConfig();
        }),
      );
      for (var rowIndex = oldCount - 1; rowIndex >= newCount; rowIndex--) {
        if (seatList.value[rowIndex].currentUser.value == null) {
          seatList.value.removeAt(rowIndex);
        } else {
          final index = seatList.value
              .indexWhere((item) => item.currentUser.value == null);
          switchSeat(
            rowIndex,
            index,
            seatList.value[rowIndex].currentUser.value!.userId,
          );
          seatList.value.removeAt(rowIndex);
        }
      }
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> takeSeat(int seatIndex, MatLiveUser user) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.seatIndex == seatIndex && seat.currentUser.value == null) {
      seat.currentUser.value = user;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> switchSeat(
    int fromSeatIndex,
    int toSeatIndex,
    String userId,
  ) async {
    kPrint(fromSeatIndex);
    kPrint(toSeatIndex);
    if (fromSeatIndex == -1 || fromSeatIndex > _maxIndex) return;
    if (toSeatIndex == -1 || toSeatIndex > _maxIndex) return;
    MatLiveUser? tempUser;

    // Store user from fromSeat
    final fromSeat = seatList.value[fromSeatIndex];
    final toSeat = seatList.value[toSeatIndex];
    tempUser = fromSeat.currentUser.value;

    if (toSeat.currentUser.value == null) {
      fromSeat.currentUser.value = null;
    } else {
      tempUser = null;
    }
    kPrint(tempUser);

    // Move user to toSeat
    if (tempUser != null) {
      toSeat.currentUser.value = tempUser;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> leaveSeat(int seatIndex, String userId) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.currentUser.value != null &&
        seat.currentUser.value?.userId == userId) {
      seat.currentUser.value = null;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> setMicOpened(int seatIndex) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.currentUser.value == null) {
      seat.isLocked.value = false;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> unLockSeat(int seatIndex) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.seatIndex == seatIndex && seat.currentUser.value == null) {
      seat.isLocked.value = false;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> lockSeat(int seatIndex) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.seatIndex == seatIndex && seat.currentUser.value == null) {
      seat.isLocked.value = true;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> muteSeat(int seatIndex) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.currentUser.value != null) {
      seat.currentUser.value!.isMicOnNotifier.value = true;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> unMuteSeat(int seatIndex) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.currentUser.value != null) {
      seat.currentUser.value!.isMicOnNotifier.value = false;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> removeUserFromSeat(int seatIndex) async {
    if (seatIndex == -1 || seatIndex > _maxIndex) return;
    final seat = seatList.value[seatIndex];
    if (seat.currentUser.value != null) {
      seat.currentUser.value = null;
      await _liveKitService.updateRoomMetadata(
        roomId: roomId,
        metadata: getSeatInfo(),
      );
    }
  }

  Future<void> _leaveSeatIfHave() async {
    final seat = seatList.value.firstWhere(
      (item) =>
          item.currentUser.value?.userId ==
          MatLiveJoinRoomManger.instance.currentUser?.userId,
    );
    seat.currentUser.value = null;
    await _liveKitService.updateRoomMetadata(
      roomId: roomId,
      metadata: getSeatInfo(),
    );
  }

  Future<void> clear() async {
    await _leaveSeatIfHave();
    seatList.value.clear();
    seatList.dispose();
    isBatchOperation = false;
    for (final subscription in subscriptions) {
      subscription.cancel();
    }
    subscriptions.clear();
  }

  String getSeatInfo() {
    List<Map<String, dynamic>> seats = [];
    for (var seat in seatList.value) {
      seats.add({
        'seatIndex': seat.seatIndex,
        'rowIndex': seat.rowIndex,
        'columnIndex': seat.columnIndex,
        'isLocked': seat.isLocked.value,
        'currentUser': seat.currentUser.value != null
            ? {
                'userId': seat.currentUser.value?.userId,
                'name': seat.currentUser.value?.name,
                'avatar': seat.currentUser.value?.avatar,
                'roomId': seat.currentUser.value?.roomId,
                'isMuted': seat.currentUser.value?.isMicOnNotifier.value,
              }
            : null,
      });
    }
    return jsonEncode({'seats': seats});
  }

  void seatsFromMetadata(String? metadata) {
    if (metadata == null || !metadata.contains('seats')) return;
    final seats = jsonDecode(metadata)['seats'];
    for (var item in seats) {
      final seat = seatList.value[item['seatIndex']];
      seat.isLocked.value = item['isLocked'];
      if (seat.currentUser.value != null && item['currentUser'] != null) {
        seat.currentUser.value!
          ..name = item['currentUser']['name']
          ..userId = item['currentUser']['userId']
          ..roomId = item['currentUser']['roomId']
          ..isMicOnNotifier.value = item['currentUser']['isMuted']
          ..avatarUrlNotifier.value = item['currentUser']['avatar'];
      } else if (item['currentUser'] != null) {
        seat.currentUser.value = MatLiveUser(
            roomId: item['currentUser']['roomId'],
            name: item['currentUser']['name'],
            userId: item['currentUser']['userId'],
            avatar: item['currentUser']['avatar'])
          ..isMicOnNotifier.value = item['currentUser']['isMuted']
          ..avatarUrlNotifier.value = item['currentUser']['avatar'];
      } else if (item['currentUser'] == null) {
        seat.currentUser.value = null;
      }
    }
  }
}
