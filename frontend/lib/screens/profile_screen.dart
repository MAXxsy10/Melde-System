// lib/screens/profile_screen.dart
import 'package:flutter/material.dart';
import '../models/models.dart';
import '../services/api_service.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});
  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final ApiService _api = ApiService();
  User? _user;
  final _nickCtrl = TextEditingController();
  final _radiusCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadProfile();
  }

  Future<void> _loadProfile() async {
    final u = await _api.getUserProfile();
    if (u != null) {
      setState(() {
        _user = u;
        _nickCtrl.text = u.nickname;
        _radiusCtrl.text = u.notificationRadius.toString();
      });
    }
  }

  Future<void> _update() async {
    final r = int.tryParse(_radiusCtrl.text);
    final u = await _api.updateUserProfile(
        nickname: _nickCtrl.text,
        radius: r
    );
    if (u != null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Profile Updated")));
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_user == null) return const Scaffold(body: Center(child: CircularProgressIndicator()));

    return Scaffold(
      appBar: AppBar(title: const Text("Profile")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            const CircleAvatar(radius: 40, child: Icon(Icons.person, size: 40)),
            const SizedBox(height: 20),
            TextField(controller: _nickCtrl, decoration: const InputDecoration(labelText: "Nickname")),
            TextField(controller: _radiusCtrl, decoration: const InputDecoration(labelText: "Radius (km)"), keyboardType: TextInputType.number),
            const SizedBox(height: 20),
            ElevatedButton(onPressed: _update, child: const Text("Save Changes")),
          ],
        ),
      ),
    );
  }
}