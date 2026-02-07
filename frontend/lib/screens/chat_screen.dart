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
  final WebSocketService _ws = WebSocketService(); // Singleton now works
  final TextEditingController _textCtrl = TextEditingController();
  final ScrollController _scrollCtrl = ScrollController();

  List<ChatMessage> _messages = [];
  int? _currentUserId;

  @override
  void initState() {
    super.initState();
    _loadUserAndMessages();
    _ws.subscribeToChat(widget.reportId);
    _ws.chatUpdates.listen((json) {
      if (_currentUserId != null) {
        final msg = ChatMessage.fromJson(json, _currentUserId!);
        if (msg.reportId == widget.reportId) {
          setState(() { _messages.add(msg); });
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
        _scrollCtrl.animateTo(
          _scrollCtrl.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final myBubbleColor = theme.colorScheme.primary.withOpacity(0.8);
    final otherBubbleColor = Colors.white;

    return Scaffold(
      appBar: AppBar(title: const Text("Team Chat")),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              controller: _scrollCtrl,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              itemCount: _messages.length,
              itemBuilder: (ctx, i) {
                final msg = _messages[i];
                return Align(
                  alignment: msg.isMe ? Alignment.centerRight : Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.symmetric(vertical: 4),
                    padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 14),
                    decoration: BoxDecoration(
                      color: msg.isMe ? myBubbleColor : otherBubbleColor,
                      borderRadius: BorderRadius.only(
                        topLeft: const Radius.circular(16),
                        topRight: const Radius.circular(16),
                        bottomLeft: msg.isMe ? const Radius.circular(16) : Radius.zero,
                        bottomRight: msg.isMe ? Radius.zero : const Radius.circular(16),
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 5,
                          offset: const Offset(0, 2),
                        )
                      ]
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        if (!msg.isMe)
                          Text(msg.senderName,
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.secondary
                            )
                          ),
                        Text(
                          msg.message,
                          style: TextStyle(
                            color: msg.isMe ? Colors.white : Colors.black87
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          DateFormat('HH:mm').format(msg.sentAt),
                          style: TextStyle(
                            fontSize: 10,
                            color: msg.isMe ? Colors.white70 : Colors.grey
                          )
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),

          // --- THE INPUT "FRAME" ---
          Container(
            padding: const EdgeInsets.all(16.0),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 10,
                  offset: const Offset(0, -5),
                )
              ],
              borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
            ),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _textCtrl,
                    decoration: InputDecoration(
                      hintText: "Type a message...",
                      filled: true,
                      fillColor: theme.scaffoldBackgroundColor,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(30),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 20),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                CircleAvatar(
                  backgroundColor: theme.colorScheme.primary,
                  radius: 24,
                  child: IconButton(
                    icon: const Icon(Icons.send, color: Colors.white, size: 20),
                    onPressed: _sendMessage,
                  ),
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}