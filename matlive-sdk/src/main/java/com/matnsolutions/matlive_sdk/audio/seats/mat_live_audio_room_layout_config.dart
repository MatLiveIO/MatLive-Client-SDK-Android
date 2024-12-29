class MatLiveAudioRoomLayoutConfig {
  MatLiveAudioRoomLayoutConfig({
    this.rowSpacing = 0,
    List<MatLiveAudioRoomLayoutRowConfig>? rowConfigs,
  }) : rowConfigs = rowConfigs ??
            [
              MatLiveAudioRoomLayoutRowConfig(),
              MatLiveAudioRoomLayoutRowConfig(),
            ];

  /// The spacing between rows, in pixels
  final double rowSpacing;

  /// Configuration of each row
  List<MatLiveAudioRoomLayoutRowConfig> rowConfigs;

  @override
  String toString() {
    return 'rowSpacing:$rowSpacing, rowConfigs:${rowConfigs.toString()}';
  }
}

/// Configuration of the row in the seat layout
class MatLiveAudioRoomLayoutRowConfig {
  MatLiveAudioRoomLayoutRowConfig({
    this.count = 5,
    this.seatSpacing = 0,
  });
  int count;

  int seatSpacing = 0;

  @override
  String toString() {
    return 'row config:{count:$count, spacing:$seatSpacing}';
  }
}
