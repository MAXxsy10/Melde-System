import 'package:flutter/material.dart';
import 'screens/auth_screen.dart';
import 'screens/home_screen.dart';
import 'services/api_service.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final ApiService _api = ApiService();
  bool _isLoggedIn = false;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _checkLogin();
  }

  Future<void> _checkLogin() async {
    final uid = await _api.getCurrentUserId();
    setState(() {
      _isLoggedIn = uid != null;
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    // 1. DEFINE THE COLOR PALETTE
    const seedColor = Color(0xFF6D4C41);
    const beigeBackground = Color(0xFFFDFBF7);

    return MaterialApp(
      title: 'Citizen Report',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        // 2. GENERATE A SMOOTH COLOR SCHEME
        colorScheme: ColorScheme.fromSeed(
          seedColor: seedColor,
          background: beigeBackground,
          surface: Colors.white, // Cards will be white
          surfaceTint: Colors.transparent, // Removes weird tints on cards
        ),
        scaffoldBackgroundColor: beigeBackground,

        // 3. GLOBAL STYLE FOR TEXT FIELDS (The "Frame" look)
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: Colors.white,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: Color(0xFF8D6E63)),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: Color(0xFFD7CCC8)), // Light brown border
          ),
          contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        ),

        // 4. GLOBAL BUTTON STYLE
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: seedColor,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            elevation: 3,
          ),
        ),
      ),
      home: _loading
          ? const Scaffold(body: Center(child: CircularProgressIndicator()))
          : (_isLoggedIn ? const HomeScreen() : const AuthScreen()),
      routes: {
        // '/'; (ctx) => const AuthScreen(),
        '/home': (ctx) => const HomeScreen(),
      },
    );
  }
}