  import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/audio/define/events.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_chat_message.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_request_take_mic.kt';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_user.dart';
import 'package:matlive_client_sdk_flutter/audio/seats/room_seat_service.dart';
import 'package:matlive_client_sdk_flutter/utils/kprint.dart';

import 'mat_live_join_room_manger.dart';

mixin LiveRoomEventReceiverManger {
  late RoomSeatService? seatService;
  late ValueNotifier<List<MatLiveChatMessage>> messages;
  late ValueNotifier<List<MatLiveRequestTakeMic>> inviteRequests;

  void receivedData(
    Map<String, dynamic> data,
    Function(int seatIndex)? onInvitedToMic,
    Function(String data)? onSendGift,
  ) {
    if (seatService == null) return;
    try {
      final int event = data['event'] as int;
      final Map<String, dynamic> user = data['user'] as Map<String, dynamic>;
      final matUser = MatLiveUser(
        userId: user['userId'],
        name: user['name'],
        avatar: user['avatar'],
        roomId: data['roomId'],
        metadata: data['metadata'],
      );
      switch (event) {
        case MatLiveEvents.sendMessage:
          messages.value = messages.value
            ..add(MatLiveChatMessage(
              user: matUser,
              message: data['message'],
              roomId: data['roomId'],
            ));
          break;
        case MatLiveEvents.clearChat:
          messages.value = [];
          break;

        case MatLiveEvents.inviteUserToTakeMic:
          if (MatLiveJoinRoomManger.instance.currentUser?.userId ==
                  data['userId'] &&
              onInvitedToMic != null) {
            onInvitedToMic(data['seatIndex']);
          }
          break;
        case MatLiveEvents.sendGift:
          if (onSendGift != null) {
            onSendGift(data['gift']);
          }
          break;

        case MatLiveEvents.requestTakeMic:
          inviteRequests.value = inviteRequests.value
            ..add(MatLiveRequestTakeMic(
                user: matUser, seatIndex: data['seatIndex']));
          break;
      }
    } catch (e) {
      kPrint('Error handling data received: $e');
    }
  }
}
