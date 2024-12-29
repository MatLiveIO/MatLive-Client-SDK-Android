import 'package:dio/dio.dart';
import 'package:matlive_client_sdk_flutter/utils/kprint.dart';

class LiveKitService {
  final Dio _dio;
  final String baseUrl;

  LiveKitService({required this.baseUrl})
      : _dio = Dio(BaseOptions(baseUrl: baseUrl));

  Future<Map<String, dynamic>> createRoom({
    required String roomId,
  }) async {
    try {
      final response = await _dio.post(
        '/livekit/create-room',
        data: {'roomName': roomId},
      );
      kPrint(response.data);
      return response.data;
    } catch (e) {
      throw Exception('Failed to create room: $e');
    }
  }

  Future<Map<String, dynamic>> updateRoomMetadata({
    required String roomId,
    required String metadata,
  }) async {
    try {
      final response = await _dio.put(
        '/livekit/room-metadata',
        data: {
          'roomId': roomId,
          'metadata': metadata,
        },
      );
      return response.data;
    } catch (e) {
      throw Exception('Failed to update metadata: $e');
    }
  }

  Future<Map<String, dynamic>> createToken({
    required String username,
    required String roomId,
  }) async {
    try {
      final response = await _dio.get(
        '/livekit/token',
        queryParameters: {
          'identity': username,
          'room': roomId,
        },
      );
      kPrint(response.data);
      return response.data;
    } catch (e) {
      throw Exception('Failed to create token: $e');
    }
  }
}
