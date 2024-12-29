import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/mat_live.dart';
import '../widgets/audio_room_layout.dart';

class AudioRoomScreen extends StatefulWidget {
  final String roomId;
  final String token;

  const AudioRoomScreen({
    super.key,
    required this.roomId,
    required this.token,
  });

  @override
  State<AudioRoomScreen> createState() => _AudioRoomScreenState();
}

class _AudioRoomScreenState extends State<AudioRoomScreen> {
  final _matLiveRoomManger = MatLiveRoomManger.instance;
  bool loading = true;

  final TextEditingController controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    _initializeRoom();
  }

  Future<void> _initializeRoom() async {
    try {
      await MatLiveJoinRoomManger.instance.init(
        onInvitedToMic: (seatIndex) {},
        onSendGift: (data) {},
      );
      await MatLiveJoinRoomManger.instance.connect(
        roomId: widget.roomId,
        token: widget.token,
        name: 'Ibrahim Nashatat',
        avatar:
            'https://img-cdn.pixlr.com/image-generator/history/65bb506dcb310754719cf81f/ede935de-1138-4f66-8ed7-44bd16efc709/medium.webp',
        userId: '10',
        metadata: '{userRome:"admin"}',
      );

      final seatService = MatLiveRoomManger.instance.seatService;
      // Initialize seat layout
      seatService!.initWithConfig(
        MatLiveAudioRoomLayoutConfig(
          rowSpacing: 16,
          rowConfigs: [
            MatLiveAudioRoomLayoutRowConfig(count: 4, seatSpacing: 12),
            MatLiveAudioRoomLayoutRowConfig(count: 4, seatSpacing: 12),
          ],
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to connect: $e')),
      );
    }
    loading = false;
    setState(() {});

    /// _matLiveRoomManger.inviteRequests;
  }

  @override
  void dispose() {
    MatLiveJoinRoomManger.instance.close();
    super.dispose();
  }

  void _sendGift(String gift) {
    _matLiveRoomManger.sendGift(gift);
    _showSnackBar('sendGift');
  }

  void _requestTakeMic(int seatIndex) {
    _matLiveRoomManger.requestTakeMic(seatIndex);
    _showSnackBar('requestTakeMic');
  }

  void _clearChat() {
    _matLiveRoomManger.clearChat();
    _showSnackBar('clearChat');
  }

  void _inviteUserToTakeMic(String userId, int seatIndex) {
    _matLiveRoomManger.inviteUserToTakeMic(userId, seatIndex);
    _showSnackBar('inviteUserToTakeMic');
  }

  void _handleTakeMic(int index) {
    _matLiveRoomManger.takeSeat(index);
    _showSnackBar('Took mic at seat $index');
  }

  void _handleMuteMic(int index) {
    final seatService = MatLiveRoomManger.instance.seatService;
    final seat = seatService!.seatList.value[index];
    if (!seat.currentUser.value!.isMicOnNotifier.value) {
      _matLiveRoomManger.muteSeat(index);
    } else {
      _matLiveRoomManger.unMuteSeat(index);
    }
    _showSnackBar(!seat.currentUser.value!.isMicOnNotifier.value
        ? 'Unmuted mic at seat $index'
        : 'Muted mic at seat $index');
  }

  void _handleRemoveSpeaker(int index) {
    _matLiveRoomManger.removeUserFromSeat(index);
    _showSnackBar('Removed speaker from seat $index');
  }

  void _handleLeaveMic(int index) {
    _matLiveRoomManger.leaveSeat(index);
    _showSnackBar('Left mic at seat $index');
  }

  void _handleLockMic(int index) {
    _matLiveRoomManger.lockSeat(index);
    _showSnackBar('Seat locked $index');
  }

  void _handleUnLockMic(int index) {
    _matLiveRoomManger.unLockSeat(index);
    _showSnackBar('Seat unlocked $index');
  }

  void _handleSwitchSeat(int toIndex) {
    _matLiveRoomManger.switchSeat(toIndex);
    _showSnackBar('Switched seat to $toIndex');
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Room: ${widget.roomId}',
          style: const TextStyle(
            color: Colors.black,
            fontSize: 16.0,
          ),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(vertical: 21.0),
        child: loading
            ? const Center(
                child: SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                  ),
                ),
              )
            : Column(
                children: [
                  Padding(
                    padding: const EdgeInsets.only(bottom: 10.0),
                    child: AudioRoomLayout(
                      onTakeMic: _handleTakeMic,
                      onMuteMic: _handleMuteMic,
                      onRemoveSpeaker: _handleRemoveSpeaker,
                      onLeaveMic: _handleLeaveMic,
                      onSwitchSeat: _handleSwitchSeat,
                      onLockMic: _handleLockMic,
                      onUnLockMic: _handleUnLockMic,
                    ),
                  ),
                  Expanded(
                    child: SingleChildScrollView(
                      padding: const EdgeInsets.symmetric(horizontal: 21.0),
                      child: ValueListenableBuilder<List<MatLiveChatMessage>>(
                        valueListenable: MatLiveRoomManger.instance.messages,
                        builder: (context, messages, _) {
                          return Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: messages.map(
                              (item) {
                                return Padding(
                                  padding: const EdgeInsets.only(bottom: 8.0),
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Row(
                                        children: [
                                          Image.network(
                                            item.user.avatar,
                                            fit: BoxFit.cover,
                                            height: 25.0,
                                            width: 25.0,
                                            errorBuilder:
                                                (context, error, stackTrace) {
                                              return const Icon(Icons.person,
                                                  size: 22.0);
                                            },
                                          ),
                                          const SizedBox(width: 12.0),
                                          Text(item.user.name),
                                        ],
                                      ),
                                      const SizedBox(height: 4.0),
                                      Text(item.message),
                                    ],
                                  ),
                                );
                              },
                            ).toList(),
                          );
                        },
                      ),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: controller,
                            decoration: const InputDecoration(
                              hintText: 'Type a message ...',
                              border: OutlineInputBorder(),
                            ),
                          ),
                        ),
                        IconButton(
                          onPressed: () {
                            if (controller.text.isNotEmpty) {
                              MatLiveRoomManger.instance
                                  .sendMessage(controller.text);
                              controller.clear();
                            }
                          },
                          icon: const Icon(Icons.send),
                        )
                      ],
                    ),
                  )
                ],
              ),
      ),
    );
  }
}
