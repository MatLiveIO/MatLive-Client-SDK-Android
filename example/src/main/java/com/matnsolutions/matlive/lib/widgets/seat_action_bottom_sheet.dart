import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/audio/define/mat_live_user.dart';

class SeatActionBottomSheet extends StatelessWidget {
  final MatLiveUser? user;
  final Function()? onTakeMic;
  final Function()? onMuteMic;
  final Function()? onRemoveSpeaker;
  final Function()? onLeaveMic;
  final Function()? onUnLockMic;
  final Function()? onLockMic;
  final Function()? onSwitch;

  const SeatActionBottomSheet({
    super.key,
    required this.user,
    this.onTakeMic,
    this.onMuteMic,
    this.onRemoveSpeaker,
    this.onLeaveMic,
    this.onUnLockMic,
    this.onLockMic,
    this.onSwitch,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 16),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 40,
            height: 4,
            margin: const EdgeInsets.only(bottom: 16),
            decoration: BoxDecoration(
              color: Colors.grey[300],
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          if (user == null) ...[
            _ActionButton(
              icon: Icons.mic,
              label: 'Take Mic',
              onTap: () {
                Navigator.pop(context);
                onTakeMic?.call();
              },
            ),_ActionButton(
              icon: Icons.swap_calls,
              label: 'Switch Mic',
              onTap: () {
                Navigator.pop(context);
                onSwitch?.call();
              },
              isDestructive: true,
            ),
            _ActionButton(
              icon: Icons.lock_outline,
              label: 'Lock Mic',
              onTap: () {
                Navigator.pop(context);
                onLockMic?.call();
              },
              isDestructive: true,
            ),
            _ActionButton(
              icon: Icons.lock_open,
              label: 'UnLock Mic',
              onTap: () {
                Navigator.pop(context);
                onUnLockMic?.call();
              },
              isDestructive: true,
            ),
          ] else ...[
            ValueListenableBuilder<bool>(
                valueListenable: user!.isMicOnNotifier,
                builder: (context, isMuted, _) {
                  return isMuted
                      ? _ActionButton(
                          icon: Icons.mic_off,
                          label: 'Mute Mic',
                          onTap: () {
                            Navigator.pop(context);
                            onMuteMic?.call();
                          },
                        )
                      : _ActionButton(
                          icon: Icons.mic,
                          label: 'UnMute Mic',
                          onTap: () {
                            Navigator.pop(context);
                            onMuteMic?.call();
                          },
                        );
                }),
            _ActionButton(
              icon: Icons.person_remove,
              label: 'Remove Speaker',
              onTap: () {
                Navigator.pop(context);
                onRemoveSpeaker?.call();
              },
              isDestructive: true,
            ),

            _ActionButton(
              icon: Icons.exit_to_app,
              label: 'Leave Mic',
              onTap: () {
                Navigator.pop(context);
                onLeaveMic?.call();
              },
              isDestructive: true,
            ),
          ],
        ],
      ),
    );
  }
}

class _ActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final bool isDestructive;

  const _ActionButton({
    required this.icon,
    required this.label,
    required this.onTap,
    this.isDestructive = false,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
        child: Row(
          children: [
            Icon(
              icon,
              color: isDestructive ? Colors.red : Colors.black87,
              size: 24,
            ),
            const SizedBox(width: 12),
            Text(
              label,
              style: TextStyle(
                fontSize: 16,
                color: isDestructive ? Colors.red : Colors.black87,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
