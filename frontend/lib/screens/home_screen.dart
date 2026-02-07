// lib/screens/home_screen.dart
import 'package:flutter/material.dart' hide Category;
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:geolocator/geolocator.dart';
import '../models/models.dart';
import '../services/api_service.dart';
import '../services/websocket_service.dart';
import 'create_report_screen.dart';
import 'report_detail_screen.dart';
import 'profile_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ApiService _api = ApiService();
  final WebSocketService _ws = WebSocketService();
  final MapController _mapController = MapController();

  List<Report> _reports = [];
  LatLng _currentPos = const LatLng(49.0069, 8.4037); // Default Karlsruhe

  @override
  void initState() {
    super.initState();
    _ws.connect();
    _ws.reportUpdates.listen((data) {
      // Refresh map when a new report notification comes in
      _loadReports();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("New Report nearby!")));
    });

    _locateUser();
  }
  void _onMapTap(TapPosition tapPosition, LatLng point) async {
    // Navigate to Create Screen using the TAPPED location
    await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => CreateReportScreen(initialPos: point))
    );
    _loadReports(); // Refresh map immediately after returning
  }

  Future<void> _locateUser() async {
    // Basic permission check (add permission_handler logic for prod)
    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }

    if (permission == LocationPermission.whileInUse || permission == LocationPermission.always) {
      Position pos = await Geolocator.getCurrentPosition();
      setState(() {
        _currentPos = LatLng(pos.latitude, pos.longitude);
      });
      _mapController.move(_currentPos, 14);
      _loadReports();
    }
  }

  Future<void> _loadReports() async {
    final reports = await _api.getReports(lat: _currentPos.latitude, lng: _currentPos.longitude, radius: 20);
    setState(() => _reports = reports);
  }

  void _logout() {
    _api.logout();
    Navigator.of(context).pushReplacementNamed('/');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Citizen Report'),
        actions: [
          IconButton(icon: const Icon(Icons.person), onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const ProfileScreen()))),
          IconButton(icon: const Icon(Icons.exit_to_app), onPressed: _logout),
        ],
      ),
      body: FlutterMap(
        mapController: _mapController,
        options: MapOptions(initialCenter: _currentPos, initialZoom: 14, onTap: _onMapTap,),
        children: [
          TileLayer(urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'),
          MarkerLayer(
            markers: _reports.map((report) => Marker(
              point: LatLng(report.location.latitude, report.location.longitude),
              width: 40,
              height: 40,
              child: GestureDetector(
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ReportDetailScreen(reportId: report.id))),
                child: _buildMarkerIcon(report.category),
              ),
            )).toList(),
          ),
          MarkerLayer(markers: [Marker(point: _currentPos, child: const Icon(Icons.my_location, color: Colors.blue))]),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add_a_photo),
        onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => CreateReportScreen(initialPos: _currentPos))),
      ),
    );
  }

  Widget _buildMarkerIcon(Category cat) {
    IconData icon;
    Color color;
    switch (cat) {
      case Category.TRASH: icon = Icons.delete; color = Colors.brown; break;
      case Category.DAMAGE: icon = Icons.broken_image; color = Colors.red; break;
      case Category.SOCIAL: icon = Icons.people; color = Colors.orange; break;
      case Category.TRAFFIC: icon = Icons.traffic; color = Colors.grey; break;
      case Category.NATURE: icon = Icons.nature; color = Colors.green; break;
      default: icon = Icons.report; color = Colors.purple;
    }
    return Container(
      decoration: const BoxDecoration(color: Colors.white, shape: BoxShape.circle),
      child: Icon(icon, color: color, size: 30),
    );
  }
}