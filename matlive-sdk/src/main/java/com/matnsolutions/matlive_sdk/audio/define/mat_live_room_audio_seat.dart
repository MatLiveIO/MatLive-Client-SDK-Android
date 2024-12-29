import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_user.dart';

class MatLiveRoomAudioSeat {
  int seatIndex = 0;
  int rowIndex = 0;
  int columnIndex = 0;
  GlobalKey seatKey = GlobalKey();
  ValueNotifier<MatLiveUser?> lastUser = ValueNotifier(null);
  ValueNotifier<MatLiveUser?> currentUser = ValueNotifier(null);
  ValueNotifier<bool> isLocked = ValueNotifier(false);

  MatLiveRoomAudioSeat(this.seatIndex, this.rowIndex, this.columnIndex);
}
