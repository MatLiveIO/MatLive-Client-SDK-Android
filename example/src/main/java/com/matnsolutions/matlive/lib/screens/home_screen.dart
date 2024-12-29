import 'package:example/consts.dart';
import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/services/livekit_service.dart';
import 'audio_room_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _livekitService = LiveKitService(baseUrl: "https://tkstg.t-wasel.com");
  bool _isCreateLoading = false;
  bool _isJoinLoading = false;

  Future<void> createRoom() async {
    if (_isCreateLoading) return;

    setState(() {
      _isCreateLoading = true;
    });

    try {
      if (!mounted) return;

      // Create a room
      final roomResponse =
          await _livekitService.createRoom(roomId: Consts.roomName);

      setState(() {
        _isCreateLoading = false;
      });
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
            content:
                Text('Room created with ID: ${roomResponse['data']['sid']}')),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isCreateLoading = false;
        });
      }
    }
  }

  Future<void> joinRoom(String roomId) async {
    if (_isJoinLoading) return;

    setState(() {
      _isJoinLoading = true;
    });

    try {
      if (!mounted) return;

      // Get token for the room
      final tokenResponse = await _livekitService.createToken(
        username: 'user_${DateTime.now().millisecondsSinceEpoch}',
        roomId: Consts.roomName,
      );

      if (!mounted) return;

      // Navigate to the audio room screen
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => AudioRoomScreen(
            roomId: roomId,
            token: tokenResponse['data'] as String,
          ),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isJoinLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('MatLive Audio Rooms'),
      ),
      body: SingleChildScrollView(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text(
                  'Welcome to MatLive Audio Rooms',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                const Text(
                  'Create a new room and start talking with others',
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.grey,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 48),
                SizedBox(
                  width: 200,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: _isCreateLoading ? null : createRoom,
                    style: ElevatedButton.styleFrom(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(25),
                      ),
                    ),
                    child: _isCreateLoading
                        ? const SizedBox(
                            width: 24,
                            height: 24,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                            ),
                          )
                        : const Text(
                            'Create Room',
                            style: TextStyle(
                              fontSize: 18,
                            ),
                          ),
                  ),
                ),
                const SizedBox(height: 48),
                SizedBox(
                  width: 200,
                  height: 50,
                  child: ElevatedButton(
                    onPressed:
                        _isJoinLoading ? null : () => joinRoom(Consts.roomName),
                    style: ElevatedButton.styleFrom(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(25),
                      ),
                    ),
                    child: _isJoinLoading
                        ? const SizedBox(
                            width: 24,
                            height: 24,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                            ),
                          )
                        : const Text(
                            'Join Room',
                            style: TextStyle(
                              fontSize: 18,
                            ),
                          ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
