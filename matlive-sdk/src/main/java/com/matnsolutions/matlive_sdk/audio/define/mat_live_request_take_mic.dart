import 'package:matlive_client_sdk_flutter/audio/define/mat_live_user.dart';

class MatLiveRequestTakeMic {
  MatLiveRequestTakeMic({
    required this.seatIndex,
    required this.user,
  });

  final int seatIndex;
  final MatLiveUser user;
}
