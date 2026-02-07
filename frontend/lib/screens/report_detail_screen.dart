// lib/screens/report_detail_screen.dart
import 'package:flutter/foundation.dart' hide Category; // Add this import for kIsWeb
import 'package:flutter/material.dart' hide Category;
import 'package:intl/intl.dart';
import '../models/models.dart';
import '../services/api_service.dart';
import 'chat_screen.dart';

// lib/screens/report_detail_screen.dart
import 'package:dio/dio.dart';

class ReportDetailScreen extends StatefulWidget {
  final int reportId;
  const ReportDetailScreen({super.key, required this.reportId});
  @override
  State<ReportDetailScreen> createState() => _ReportDetailScreenState();
}

class _ReportDetailScreenState extends State<ReportDetailScreen> {
  final ApiService _api = ApiService();
  Report? _report;
  List<ReportHistory> _history = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    try {
      final report = await _api.getReportDetail(widget.reportId);
      final history = await _api.getReportHistory(widget.reportId);
      if (mounted) {
        setState(() {
          _report = report;
          _history = history;
          _isLoading = false;
        });
      }
    } catch (e) {
      print("Error loading data: $e");
    }
  }

  Future<void> _joinReport() async {
    try {
      final success = await _api.joinReport(widget.reportId);
      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Joined as helper!")));
        _loadData();
      }
    } on DioException catch (e) {
      String msg = "Could not join report";
      if (e.response != null && e.response!.data is Map) {
        msg = e.response!.data['message'] ?? msg;
      }
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg), backgroundColor: Colors.orange));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading || _report == null) return const Scaffold(body: Center(child: CircularProgressIndicator()));

    final bool canChat = _report!.currentUserIsHelper || (_report!.currentUserIsCreator == true);

    final String baseUrl = kIsWeb ? 'http://localhost:8080' : 'http://10.0.2.2:8080';

    return Scaffold(
      appBar: AppBar(title: Text(_report!.title)),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (_report!.photoUrl != null)
              Image.network(
                '$baseUrl${_report!.photoUrl!}',
                height: 200,
                width: double.infinity,
                fit: BoxFit.cover,
                errorBuilder: (ctx, err, _) => Container(
                    height: 200,
                    color: Colors.grey[300],
                    child: const Center(child: Icon(Icons.broken_image, size: 50))
                ),
              ),

            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Chip(label: Text(_report!.category.name)),
                      Chip(
                        label: Text(_report!.status.name),
                        backgroundColor: _getStatusColor(_report!.status),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text(_report!.description ?? "No description", style: const TextStyle(fontSize: 16)),
                  const SizedBox(height: 20),
                  Text("Helpers: ${_report!.helperCount}", style: const TextStyle(fontWeight: FontWeight.bold)),

                  const Divider(),

                  if (!_report!.currentUserIsHelper && (_report!.currentUserIsCreator != true))
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        icon: const Icon(Icons.handshake),
                        label: const Text("I want to help"),
                        onPressed: _joinReport,
                      ),
                    ),

                  if (canChat)
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        icon: const Icon(Icons.chat),
                        label: const Text("Open Chat"),
                        onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ChatScreen(reportId: widget.reportId))),
                        style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
                      ),
                    ),

                  const Divider(),
                  const Text("History", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  ..._history.map((h) => ListTile(
                    leading: const Icon(Icons.history),
                    title: Text(h.status.name),
                    subtitle: Text("${h.changedBy} - ${DateFormat('dd.MM HH:mm').format(h.timestamp)}"),
                  )),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getStatusColor(ReportStatus status) {
    switch (status) {
      case ReportStatus.NEW: return Colors.blue[100]!;
      case ReportStatus.RESOLVED: return Colors.green[100]!;
      case ReportStatus.CLOSED: return Colors.grey[300]!;
      default: return Colors.orange[100]!;
    }
  }
}
/*
class ReportDetailScreen extends StatefulWidget {
  final int reportId;
  const ReportDetailScreen({super.key, required this.reportId});
  @override
  State<ReportDetailScreen> createState() => _ReportDetailScreenState();
}

class _ReportDetailScreenState extends State<ReportDetailScreen> {
  final ApiService _api = ApiService();
  Report? _report;
  List<ReportHistory> _history = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    try {
      final report = await _api.getReportDetail(widget.reportId);
      final history = await _api.getReportHistory(widget.reportId);
      if (mounted) {
        setState(() {
          _report = report;
          _history = history;
          _isLoading = false;
        });
      }
    } catch (e) {
      print("Error loading details: $e");
    }
  }

  Future<void> _joinReport() async {
    final success = await _api.joinReport(widget.reportId);
    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Joined as helper!")));
      _loadData(); // Refresh to update UI state
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading || _report == null) return const Scaffold(body: Center(child: CircularProgressIndicator()));

    final bool canChat = _report!.currentUserIsHelper || (_report!.currentUserIsCreator == true);

    // FIX: URL Logic
    final String baseUrl = kIsWeb ? 'http://localhost:8080' : 'http://10.0.2.2:8080';

    return Scaffold(
      appBar: AppBar(title: Text(_report!.title)),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (_report!.photoUrl != null)
              Image.network('$baseUrl${_report!.photoUrl!}',
                  height: 200, width: double.infinity, fit: BoxFit.cover,
                  errorBuilder: (ctx, _, __) => const SizedBox(height: 200, child: Center(child: Icon(Icons.broken_image)))),

            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Chip(label: Text(_report!.category.name)),
                      Chip(
                        label: Text(_report!.status.name),
                        backgroundColor: _getStatusColor(_report!.status),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text(_report!.description ?? "No description", style: const TextStyle(fontSize: 16)),
                  const SizedBox(height: 20),
                  Text("Helpers: ${_report!.helperCount}", style: const TextStyle(fontWeight: FontWeight.bold)),

                  const Divider(),

                  // Action Buttons
                  if (!_report!.currentUserIsHelper && (_report!.currentUserIsCreator != true))
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        icon: const Icon(Icons.handshake),
                        label: const Text("I want to help"),
                        onPressed: _joinReport,
                      ),
                    ),

                  if (canChat)
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        icon: const Icon(Icons.chat),
                        label: const Text("Open Chat"),
                        onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ChatScreen(reportId: widget.reportId))),
                        style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
                      ),
                    ),

                  const Divider(),
                  const Text("History", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  ..._history.map((h) => ListTile(
                    leading: const Icon(Icons.history),
                    title: Text(h.status.name),
                    subtitle: Text("${h.changedBy} - ${DateFormat('dd.MM HH:mm').format(h.timestamp)}"),
                  )),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getStatusColor(ReportStatus status) {
    switch (status) {
      case ReportStatus.NEW: return Colors.blue[100]!;
      case ReportStatus.RESOLVED: return Colors.green[100]!;
      case ReportStatus.CLOSED: return Colors.grey[300]!;
      default: return Colors.orange[100]!;
    }
  }
}*/