// lib/screens/create_report_screen.dart
import 'dart:io';
import 'package:flutter/foundation.dart' hide Category; // For kIsWeb
import 'package:flutter/material.dart' hide Category;
import 'package:image_picker/image_picker.dart';
import 'package:latlong2/latlong.dart';
import '../models/models.dart';
import '../services/api_service.dart';



class CreateReportScreen extends StatefulWidget {
  final LatLng initialPos;
  const CreateReportScreen({super.key, required this.initialPos});
  @override
  State<CreateReportScreen> createState() => _CreateReportScreenState();
}

class _CreateReportScreenState extends State<CreateReportScreen> {
  final _formKey = GlobalKey<FormState>();
  final ApiService _api = ApiService();

  String _title = '';
  String _description = '';
  Category _category = Category.OTHER;
  XFile? _photo;
  bool _isLoading = false;

  Future<void> _pickImage() async {
    final ImagePicker picker = ImagePicker();
    final XFile? image = await picker.pickImage(source: ImageSource.gallery);
    setState(() => _photo = image);
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    _formKey.currentState!.save();

    setState(() => _isLoading = true);
    final success = await _api.createReport(
        _title,
        _description,
        _category,
        widget.initialPos.latitude,
        widget.initialPos.longitude,
        _photo
    );
    setState(() => _isLoading = false);

    if (success && mounted) {
      Navigator.pop(context);
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Failed to create report")));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("New Report")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: ListView(
            children: [
              TextFormField(
                decoration: const InputDecoration(labelText: 'Title'),
                validator: (v) => v!.isEmpty ? 'Required' : null,
                onSaved: (v) => _title = v!,
              ),
              TextFormField(
                decoration: const InputDecoration(labelText: 'Description'),
                maxLines: 3,
                onSaved: (v) => _description = v ?? '',
              ),
              DropdownButtonFormField<Category>(
                value: _category,
                decoration: const InputDecoration(labelText: 'Category'),
                items: Category.values.map((c) => DropdownMenuItem(value: c, child: Text(c.name))).toList(),
                onChanged: (v) => setState(() => _category = v!),
              ),
              const SizedBox(height: 20),

              // Photo Picker
              GestureDetector(
                onTap: _pickImage,
                child: Container(
                  height: 150,
                  color: Colors.grey[200],
                  child: _photo == null
                      ? const Center(child: Icon(Icons.camera_alt, size: 50))
                      : (kIsWeb
                  // FIX: Use network image for Web blob URLs
                      ? Image.network(_photo!.path, fit: BoxFit.cover)
                      : Image.file(File(_photo!.path), fit: BoxFit.cover)),
                ),
              ),

              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _isLoading ? null : _submit,
                child: _isLoading ? const CircularProgressIndicator() : const Text("Submit Report"),
              )
            ],
          ),
        ),
      ),
    );
  }
}
/*
class CreateReportScreen extends StatefulWidget {
  final LatLng initialPos;
  const CreateReportScreen({super.key, required this.initialPos});
  @override
  State<CreateReportScreen> createState() => _CreateReportScreenState();
}

class _CreateReportScreenState extends State<CreateReportScreen> {
  final _formKey = GlobalKey<FormState>();
  final ApiService _api = ApiService();

  String _title = '';
  String _description = '';
  Category _category = Category.OTHER;
  XFile? _photo;
  bool _isLoading = false;

  Future<void> _pickImage() async {
    final ImagePicker picker = ImagePicker();
    final XFile? image = await picker.pickImage(source: ImageSource.gallery);
    setState(() => _photo = image);
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    _formKey.currentState!.save();

    setState(() => _isLoading = true);
    final success = await _api.createReport(
        _title,
        _description,
        _category,
        widget.initialPos.latitude,
        widget.initialPos.longitude,
        _photo
    );
    setState(() => _isLoading = false);

    if (success && mounted) {
      Navigator.pop(context);
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Failed to create report")));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("New Report")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: ListView(
            children: [
              TextFormField(
                decoration: const InputDecoration(labelText: 'Title'),
                validator: (v) => v!.isEmpty ? 'Required' : null,
                onSaved: (v) => _title = v!,
              ),
              TextFormField(
                decoration: const InputDecoration(labelText: 'Description'),
                maxLines: 3,
                onSaved: (v) => _description = v ?? '',
              ),
              DropdownButtonFormField<Category>(
                value: _category,
                decoration: const InputDecoration(labelText: 'Category'),
                items: Category.values.map((c) => DropdownMenuItem(value: c, child: Text(c.name))).toList(),
                onChanged: (v) => setState(() => _category = v!),
              ),
              const SizedBox(height: 20),

              // Photo Picker
              GestureDetector(
                onTap: _pickImage,
                child: Container(
                  height: 150,
                  color: Colors.grey[200],
                  child: _photo == null
                      ? const Center(child: Icon(Icons.camera_alt, size: 50))
                      : (kIsWeb
                          ? Image.network(_photo!.path, fit: BoxFit.cover)
                          : Image.file(File(_photo!.path), fit: BoxFit.cover)),
                ),
              ),

              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _isLoading ? null : _submit,
                child: _isLoading ? const CircularProgressIndicator() : const Text("Submit Report"),
              )
            ],
          ),
        ),
      ),
    );
  }
}*/