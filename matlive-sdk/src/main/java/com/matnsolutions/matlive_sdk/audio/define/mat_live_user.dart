import 'package:flutter/material.dart';

class MatLiveUser {
  MatLiveUser({
    required this.userId,
    required this.name,
    required this.avatar,
    required this.roomId,
    this.metadata,
  });

  late String userId;
  late String name;
  late String avatar;
  late String roomId;
  String? metadata;

  String? streamID;
  int viewID = -1;
  ValueNotifier<Widget?> videoViewNotifier = ValueNotifier(null);
  ValueNotifier<bool> isCamerOnNotifier = ValueNotifier(false);
  ValueNotifier<bool> isMicOnNotifier = ValueNotifier(true);
  late ValueNotifier<String?> avatarUrlNotifier = ValueNotifier(avatar);
}

enum MatLiveUserRole {
  audience,
  coHost,
  host,
}
