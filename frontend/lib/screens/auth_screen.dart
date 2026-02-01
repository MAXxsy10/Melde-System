// lib/screens/auth_screen.dart
import 'package:flutter/material.dart';
import '../services/api_service.dart';
import 'home_screen.dart';
// lib/screens/auth_screen.dart
import 'package:dio/dio.dart'; // Import Dio for error handling

class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key});
  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final ApiService _api = ApiService();

  final _emailCtrl = TextEditingController();
  final _passCtrl = TextEditingController();
  final _nickCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  void _navigateHome() {
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const HomeScreen()),
    );
  }

  Future<void> _submit() async {
    final email = _emailCtrl.text;
    final pass = _passCtrl.text;
    final nick = _nickCtrl.text;

    try {
      if (_tabController.index == 0) {
        // Login
        await _api.login(email, pass);
        _navigateHome();
      } else {
        // Register
        await _api.register(email, pass, nick);
        _navigateHome();
      }
    } on DioException catch (e) {
      // Show the actual error message from backend
      String msg = "Action failed";
      if (e.response != null && e.response!.data is Map) {
        msg = e.response!.data['message'] ?? msg;
      }
      _showError(msg);
    } catch (e) {
      _showError("An unexpected error occurred");
    }
  }

  void _showError(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: Colors.red,
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Melde-System'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [Tab(text: 'Login'), Tab(text: 'Register')],
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(controller: _emailCtrl, decoration: const InputDecoration(labelText: 'Email')),
            TextField(controller: _passCtrl, decoration: const InputDecoration(labelText: 'Password'), obscureText: true),

            AnimatedBuilder(
              animation: _tabController,
              builder: (ctx, _) => _tabController.index == 1
                  ? TextField(controller: _nickCtrl, decoration: const InputDecoration(labelText: 'Nickname'))
                  : const SizedBox.shrink(),
            ),

            const SizedBox(height: 20),
            ElevatedButton(onPressed: _submit, child: const Text('Submit')),
            TextButton(
              onPressed: () async {
                if (await _api.loginAnonymous() != null) _navigateHome();
              },
              child: const Text('Continue Anonymously'),
            ),
          ],
        ),
      ),
    );
  }
}
/*
class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key});
  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final ApiService _api = ApiService();

  final _emailCtrl = TextEditingController();
  final _passCtrl = TextEditingController();
  final _nickCtrl = TextEditingController(); // For register

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  void _navigateHome() {
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const HomeScreen()),
    );
  }

  Future<void> _submit() async {
    final email = _emailCtrl.text;
    final pass = _passCtrl.text;
    final nick = _nickCtrl.text;

    if (_tabController.index == 0) {
      // Login
      final res = await _api.login(email, pass);
      if (res != null) _navigateHome();
      else _showError("Login failed");
    } else {
      // Register
      final res = await _api.register(email, pass, nick);
      if (res != null) _navigateHome();
      else _showError("Registration failed");
    }
  }

  void _showError(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Melde-System'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [Tab(text: 'Login'), Tab(text: 'Register')],
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(controller: _emailCtrl, decoration: const InputDecoration(labelText: 'Email')),
            TextField(controller: _passCtrl, decoration: const InputDecoration(labelText: 'Password'), obscureText: true),

            // Only show Nickname field if on Register tab
            AnimatedBuilder(
              animation: _tabController,
              builder: (ctx, _) => _tabController.index == 1
                  ? TextField(controller: _nickCtrl, decoration: const InputDecoration(labelText: 'Nickname'))
                  : const SizedBox.shrink(),
            ),

            const SizedBox(height: 20),
            ElevatedButton(onPressed: _submit, child: const Text('Submit')),
            TextButton(
              onPressed: () async {
                if (await _api.loginAnonymous() != null) _navigateHome();
              },
              child: const Text('Continue Anonymously'),
            ),
          ],
        ),
      ),
    );
  }
}*/