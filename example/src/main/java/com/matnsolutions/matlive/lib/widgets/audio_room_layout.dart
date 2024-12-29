import 'package:example/widgets/seat_action_bottom_sheet.dart';
import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/mat_live.dart';

class AudioRoomLayout extends StatelessWidget {
  final Function(int index)? onTakeMic;
  final Function(int index)? onMuteMic;
  final Function(int index)? onRemoveSpeaker;
  final Function(int index)? onLeaveMic;
  final Function(int index)? onUnLockMic;
  final Function(int index)? onLockMic;
  final Function(int toIndex)? onSwitchSeat;

  const AudioRoomLayout({
    super.key,
    this.onTakeMic,
    this.onMuteMic,
    this.onRemoveSpeaker,
    this.onLeaveMic,
    this.onLockMic,
    this.onUnLockMic,
    required this.onSwitchSeat,
  });

  @override
  Widget build(BuildContext context) {
    if (MatLiveRoomManger.instance.seatService == null) return Container();
    return ValueListenableBuilder<List<MatLiveRoomAudioSeat>>(
      valueListenable: MatLiveRoomManger.instance.seatService!.seatList,
      builder: (context, seats, _) {
        final layoutConfig =
            MatLiveRoomManger.instance.seatService!.layoutConfig!;
        return Column(
          mainAxisSize: MainAxisSize.min,
          children: List.generate(
            layoutConfig.rowConfigs.length,
            (rowIndex) {
              final rowConfig = layoutConfig.rowConfigs[rowIndex];
              return Padding(
                padding: EdgeInsets.only(bottom: layoutConfig.rowSpacing),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: List.generate(
                    rowConfig.count,
                    (seatIndex) {
                      final globalIndex =
                          _calculateGlobalIndex(rowIndex, seatIndex);
                      final seat = seats[globalIndex];
                      return Padding(
                        padding: EdgeInsets.symmetric(
                          horizontal: rowConfig.seatSpacing / 2,
                        ),
                        child: GestureDetector(
                          onTap: () => _showSeatActions(
                            context,
                            globalIndex,
                            user: seat.currentUser.value,
                          ),
                          child: ValueListenableBuilder<bool>(
                            valueListenable: seat.isLocked,
                            builder: (context, isLocked, _) {
                              if (isLocked) {
                                return Container(
                                  width: 50,
                                  height: 50,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: Colors.grey[300],
                                  ),
                                  child: const Icon(Icons.lock, size: 22.0),
                                );
                              }
                              return SeatWidget(
                                seat: seat,
                              );
                            },
                          ),
                        ),
                      );
                    },
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }

  void _showSeatActions(
    BuildContext context,
    int seatIndex, {
    required MatLiveUser? user,
  }) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => SeatActionBottomSheet(
        user: user,
        onTakeMic: onTakeMic != null ? () => onTakeMic!(seatIndex) : null,
        onMuteMic: onMuteMic != null ? () => onMuteMic!(seatIndex) : null,
        onRemoveSpeaker:
            onRemoveSpeaker != null ? () => onRemoveSpeaker!(seatIndex) : null,
        onLeaveMic: onLeaveMic != null ? () => onLeaveMic!(seatIndex) : null,
        onLockMic: onLockMic != null ? () => onLockMic!(seatIndex) : null,
        onUnLockMic: onUnLockMic != null ? () => onUnLockMic!(seatIndex) : null,
        onSwitch: onSwitchSeat != null ? () => onSwitchSeat!(seatIndex) : null,
      ),
    );
  }

  int _calculateGlobalIndex(int rowIndex, int seatIndex) {
    final layoutConfig = MatLiveRoomManger.instance.seatService!.layoutConfig!;
    int totalPreviousSeats = 0;
    for (int i = 0; i < rowIndex; i++) {
      totalPreviousSeats += layoutConfig.rowConfigs[i].count;
    }
    return totalPreviousSeats + seatIndex;
  }
}

class SeatWidget extends StatelessWidget {
  final MatLiveRoomAudioSeat seat;

  const SeatWidget({
    super.key,
    required this.seat,
  });

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<MatLiveUser?>(
        valueListenable: seat.currentUser,
        builder: (context, user, _) {
          return SizedBox(
            width: 50,
            height: 50,
            child: Stack(
              children: [
                // User avatar
                Positioned.fill(
                  child: Container(
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: Colors.grey[300],
                    ),
                    child: user != null
                        ? ClipOval(
                            child: ValueListenableBuilder<String?>(
                                valueListenable: user.avatarUrlNotifier,
                                builder: (context, avatarUrl, _) {
                                  return Image.network(
                                    '$avatarUrl',
                                    fit: BoxFit.cover,
                                    errorBuilder: (context, error, stackTrace) {
                                      return const Icon(Icons.person,
                                          size: 22.0);
                                    },
                                  );
                                }),
                          )
                        : const Icon(Icons.mic, size: 22.0),
                  ),
                ),
                // Mic icon
                if (user != null)
                  Positioned(
                    bottom: 0,
                    right: 0,
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.1),
                            blurRadius: 4,
                            offset: const Offset(0, 2),
                          ),
                        ],
                      ),
                      child: ValueListenableBuilder<bool>(
                          valueListenable: user!.isMicOnNotifier,
                          builder: (context, isMuted, _) {
                            return Icon(
                              isMuted ? Icons.mic : Icons.mic_off,
                              size: 12.0,
                              color: isMuted ? Colors.green : Colors.red,
                            );
                          }),
                    ),
                  ),
              ],
            ),
          );
        });
  }
}
