// lib/services/websocket_service.dart
import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';


class WebSocketService {
  StompClient? _client;
  final _storage = const FlutterSecureStorage();

 final String _wsUrl = kIsWeb 
      ? 'ws://localhost:8080/ws'    // Web/Edge
      : 'ws://10.0.2.2:8080/ws';

  // Streams for the UI to listen to
  final _reportStreamController = StreamController<dynamic>.broadcast();
  Stream<dynamic> get reportUpdates => _reportStreamController.stream;

  final _chatStreamController = StreamController<dynamic>.broadcast();
  Stream<dynamic> get chatUpdates => _chatStreamController.stream;

  void connect() async {
    final token = await _storage.read(key: 'jwt_token');
    if (token == null) return;

    _client = StompClient(
      config: StompConfig(
        url: _wsUrl,
        onConnect: _onConnect,
        onWebSocketError: (error) => print('WS Error: $error'),
        stompConnectHeaders: {'Authorization': 'Bearer $token'},
        webSocketConnectHeaders: {'Authorization': 'Bearer $token'},
      ),
    );
    _client?.activate();
  }

  void _onConnect(StompFrame frame) {
    print('WebSocket Connected');

    // Listen for NEW reports globally
    _client?.subscribe(
      destination: '/topic/reports',
      callback: (frame) {
        if (frame.body != null) {
          _reportStreamController.add(jsonDecode(frame.body!));
        }
      },
    );
  }

  // Call this when entering the Map screen or Detail screen
  void subscribeToReport(int reportId) {
    _client?.subscribe(
      destination: '/topic/report/$reportId',
      callback: (frame) {
        if (frame.body != null) {
          // Contains status updates or helper joined events
          _reportStreamController.add(jsonDecode(frame.body!));
        }
      },
    );
  }

  // Call this when entering Chat Screen
  void subscribeToChat(int reportId) {
    _client?.subscribe(
      destination: '/topic/report/$reportId/chat',
      callback: (frame) {
        if (frame.body != null) {
          _chatStreamController.add(jsonDecode(frame.body!));
        }
      },
    );
  }

  void deactivate() {
    _client?.deactivate();
  }
}