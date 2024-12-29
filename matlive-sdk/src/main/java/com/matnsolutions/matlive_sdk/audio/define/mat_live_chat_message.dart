import 'package:matlive_client_sdk_flutter/audio/define/mat_live_user.dart';

class MatLiveChatMessage {
  String? roomId;
  String message;
  MatLiveUser user;
  
  MatLiveChatMessage({
    required this.user,
    required this.message,
    required this.roomId,
  });
}
