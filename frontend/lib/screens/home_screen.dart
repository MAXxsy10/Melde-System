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

  // NEU: Variable für den aktuellen Radius (Standard: 20 km)
  double _currentRadius = 20.0;

  @override
  void initState() {
    super.initState();
    _ws.connect();
    _ws.reportUpdates.listen((data) {
      _loadReports();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("New Report nearby!")));
    });

    _locateUser();
  }

  void _onMapTap(TapPosition tapPosition, LatLng point) async {
    await Navigator.push(
        context,
        MaterialPageRoute(builder: (_) => CreateReportScreen(initialPos: point))
    );
    _loadReports();
  }

  Future<void> _locateUser() async {
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

  // GEÄNDERT: Nutzt nun die Variable _currentRadius statt der festen 20
  Future<void> _loadReports() async {
    final reports = await _api.getReports(
        lat: _currentPos.latitude,
        lng: _currentPos.longitude,
        radius: _currentRadius.toInt() // Hier wird der dynamische Wert übergeben
    );
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
      body: Stack(
        children: [
          FlutterMap(
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

          // NEU: Ein Slider-Widget über der Karte, um den Radius zu verändern
          Positioned(
            top: 10,
            left: 10,
            right: 10,
            child: Card(
              color: Colors.white.withOpacity(0.9),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                child: Row(
                  children: [
                    Text('Radius: ${_currentRadius.toInt()} km', style: const TextStyle(fontWeight: FontWeight.bold)),
                    Expanded(
                      child: Slider(
                        value: _currentRadius,
                        min: 1,
                        max: 100,
                        divisions: 99,
                        label: '${_currentRadius.toInt()} km',
                        onChanged: (value) {
                          setState(() {
                            _currentRadius = value; // Aktualisiert den Wert (für die UI)
                          });
                        },
                        onChangeEnd: (value) {
                          // Lädt die Reports neu, sobald der Nutzer den Slider loslässt
                          _loadReports();
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
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