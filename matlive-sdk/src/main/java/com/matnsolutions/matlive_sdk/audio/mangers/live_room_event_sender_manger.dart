import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:matlive_client_sdk_flutter/audio/define/events.dart';
import 'package:matlive_client_sdk_flutter/audio/mangers/mat_live_room_manger.dart';
import 'package:matlive_client_sdk_flutter/audio/mangers/mat_live_join_room_manger.dart';

class LiveRoomEventSenderManger {
  Future<void> _publish({required Map<String, dynamic> data}) async {
    final room = MatLiveRoomManger.instance.room;
    if (room != null) {
      data['user'] = {
        'userId': MatLiveJoinRoomManger.instance.currentUser?.userId,
        'name': MatLiveJoinRoomManger.instance.currentUser?.name,
        'avatar': MatLiveJoinRoomManger.instance.currentUser?.avatar,
      };
      data['roomId'] = room.name;
      Uint8List decoded = utf8.encode(jsonEncode(data));
      await room.localParticipant?.publishData(decoded, reliable: false);
    }
  }

  Future<void> sendMessage(String message) async {
    await _publish(data: {
      'event': MatLiveEvents.sendMessage,
      'message': message,
    });
  }

  Future<void> sendGift(String gift) async {
    await _publish(data: {
      'event': MatLiveEvents.sendGift,
      'gift': gift,
    });
  }

  Future<void> clearChat() async {
    await _publish(data: {
      'event': MatLiveEvents.clearChat,
    });
  }

  Future<void> inviteUserToTakeMic(String userId, int seatIndex) async {
    await _publish(data: {
      'event': MatLiveEvents.inviteUserToTakeMic,
      'seatIndex': seatIndex,
      'userId': userId,
    });
  }

  Future<void> requestTakeMic(int seatIndex) async {
    await _publish(data: {
      'event': MatLiveEvents.requestTakeMic,
      'seatIndex': seatIndex,
    });
  }
}
