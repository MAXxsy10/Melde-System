import 'package:flutter/material.dart';
import '../services/api_service.dart';

class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key});
  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  bool _isLogin = true;
  final _formKey = GlobalKey<FormState>();
  String _email = '';
  String _password = '';
  String _nickname = '';
  bool _isLoading = false;
  final ApiService _api = ApiService();

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    _formKey.currentState!.save();
    setState(() => _isLoading = true);

    dynamic response;
    if (_isLogin) {
      response = await _api.login(_email, _password);
    } else {
      response = await _api.register(_email, _password, _nickname);
    }

    setState(() => _isLoading = false);

    if (response != null && mounted) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_isLogin ? "Login Failed" : "Registration Failed")),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    // Using the theme colors we set in main.dart
    final primaryColor = Theme.of(context).colorScheme.primary;

    return Scaffold(
      // No AppBar, just a clean background
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Logo or Icon
              Icon(Icons.shield_outlined, size: 80, color: primaryColor),
              const SizedBox(height: 20),
              Text(
                "Citizen Report",
                style: TextStyle(
                  fontSize: 28, 
                  fontWeight: FontWeight.bold, 
                  color: primaryColor
                ),
              ),
              const SizedBox(height: 40),

              // --- THE "FRAME" (Card) ---
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: primaryColor.withOpacity(0.15),
                      blurRadius: 20,
                      offset: const Offset(0, 10),
                    )
                  ],
                ),
                padding: const EdgeInsets.all(24),
                child: Form(
                  key: _formKey,
                  child: Column(
                    children: [
                      Text(
                        _isLogin ? "Welcome Back" : "Create Account",
                        style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w600),
                      ),
                      const SizedBox(height: 24),
                      TextFormField(
                        decoration: const InputDecoration(labelText: 'Email', prefixIcon: Icon(Icons.email_outlined)),
                        keyboardType: TextInputType.emailAddress,
                        validator: (v) => v!.contains('@') ? null : 'Invalid email',
                        onSaved: (v) => _email = v!,
                      ),
                      const SizedBox(height: 16),
                      if (!_isLogin) ...[
                        TextFormField(
                          decoration: const InputDecoration(labelText: 'Nickname', prefixIcon: Icon(Icons.person_outline)),
                          validator: (v) => v!.isEmpty ? 'Required' : null,
                          onSaved: (v) => _nickname = v!,
                        ),
                        const SizedBox(height: 16),
                      ],
                      TextFormField(
                        decoration: const InputDecoration(labelText: 'Password', prefixIcon: Icon(Icons.lock_outline)),
                        obscureText: true,
                        textInputAction: TextInputAction.done,
                        onFieldSubmitted: (_) => _submit(),
                        validator: (v) => v!.length < 6 ? 'Min 6 chars' : null,
                        onSaved: (v) => _password = v!,
                      ),
                      const SizedBox(height: 30),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed: _isLoading ? null : _submit,
                          child: _isLoading 
                            ? const CircularProgressIndicator(color: Colors.white) 
                            : Text(_isLogin ? "Login" : "Register"),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              
              const SizedBox(height: 20),
              TextButton(
                onPressed: () => setState(() => _isLogin = !_isLogin),
                child: Text(_isLogin ? "Create new account" : "I already have an account"),
              ),
              TextButton(
                onPressed: () async {
                  await _api.loginAnonymous();
                  if (mounted) Navigator.pushReplacementNamed(context, '/home');
                },
                child: const Text("Continue as Guest"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}