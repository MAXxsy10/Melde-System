// lib/screens/chat_screen.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/models.dart';
import '../services/api_service.dart';
import '../services/websocket_service.dart';

class ChatScreen extends StatefulWidget {
  final int reportId;
  const ChatScreen({super.key, required this.reportId});
  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final ApiService _api = ApiService();
  final WebSocketService _ws = WebSocketService();
  final TextEditingController _textCtrl = TextEditingController();
  final ScrollController _scrollCtrl = ScrollController();

  List<ChatMessage> _messages = [];
  int? _currentUserId;

  @override
  void initState() {
    super.initState();
    _loadUserAndMessages();

    // Subscribe to WS Chat updates
    _ws.subscribeToChat(widget.reportId);
    _ws.chatUpdates.listen((json) {
      if (_currentUserId != null) {
        // We re-parse to ensure 'isMe' is calculated correctly using currentUserId
        final msg = ChatMessage.fromJson(json, _currentUserId!);
        if (msg.reportId == widget.reportId) {
          setState(() {
            _messages.add(msg);
          });
          _scrollToBottom();
        }
      }
    });
  }

  Future<void> _loadUserAndMessages() async {
    _currentUserId = await _api.getCurrentUserId();
    final msgs = await _api.getChatMessages(widget.reportId);
    setState(() => _messages = msgs);
    _scrollToBottom();
  }

  void _sendMessage() async {
    if (_textCtrl.text.isEmpty) return;
    await _api.sendChatMessageHttp(widget.reportId, _textCtrl.text);
    _textCtrl.clear();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollCtrl.hasClients) {
        _scrollCtrl.jumpTo(_scrollCtrl.position.maxScrollExtent);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Chat")),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              controller: _scrollCtrl,
              itemCount: _messages.length,
              itemBuilder: (ctx, i) {
                final msg = _messages[i];
                return Align(
                  alignment: msg.isMe ? Alignment.centerRight : Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: msg.isMe ? Colors.blue[100] : Colors.grey[200],
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        if (!msg.isMe) Text(msg.senderName, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold)),
                        Text(msg.message),
                        Text(DateFormat('HH:mm').format(msg.sentAt), style: const TextStyle(fontSize: 10, color: Colors.grey)),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(child: TextField(controller: _textCtrl, decoration: const InputDecoration(hintText: "Message..."))),
                IconButton(icon: const Icon(Icons.send), onPressed: _sendMessage),
              ],
            ),
          )
        ],
      ),
    );
  }
}