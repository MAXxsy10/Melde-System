// lib/services/api_service.dart
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' hide Category; // Important: hide Category
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:image_picker/image_picker.dart';
import '../models/models.dart';

class ApiService {
  // Use 10.0.2.2 for Android Emulator, localhost for iOS/Web
  static final String _baseUrl = kIsWeb
      ? 'http://localhost:8080/api'
      : 'http://10.0.2.2:8080/api';

  final Dio _dio = Dio(BaseOptions(baseUrl: _baseUrl));
  final FlutterSecureStorage _storage = const FlutterSecureStorage();
  
  // --- UNCOMMENTED AND FIXED SINGLETON PATTERN ---
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;

  ApiService._internal() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _storage.read(key: 'jwt_token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
    ));
  }
  // -----------------------------------------------

  Future<AuthResponse?> login(String email, String password) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'email': email,
        'password': password
      });
      return _handleAuthResponse(response);
    } catch (e) {
      print('Login error: $e');
      return null;
    }
  }

  Future<AuthResponse?> register(String email, String password, String nickname) async {
    try {
      final response = await _dio.post('/auth/register', data: {
        'email': email,
        'password': password,
        'nickname': nickname,
        'isAnonymous': false
      });
      return _handleAuthResponse(response);
    } catch (e) {
      print('Register error: $e');
      return null;
    }
  }

  Future<AuthResponse?> loginAnonymous() async {
    try {
      final response = await _dio.post('/auth/anonymous');
      return _handleAuthResponse(response);
    } catch (e) {
      return null;
    }
  }

  Future<AuthResponse> _handleAuthResponse(Response response) async {
    final data = response.data['data'];
    final authResponse = AuthResponse.fromJson(data);
    await _storage.write(key: 'jwt_token', value: authResponse.token);
    await _storage.write(key: 'user_id', value: authResponse.user.id.toString());
    return authResponse;
  }

  Future<void> logout() async {
    await _storage.deleteAll();
  }

  Future<int?> getCurrentUserId() async {
    final id = await _storage.read(key: 'user_id');
    return id != null ? int.parse(id) : null;
  }

  Future<User?> getUserProfile() async {
    final response = await _dio.get('/users/me');
    return User.fromJson(response.data['data']);
  }

  Future<User?> updateUserProfile({String? nickname, int? radius, String? lang}) async {
    final Map<String, dynamic> data = {};
    if (nickname != null) data['nickname'] = nickname;
    if (radius != null) data['notificationRadius'] = radius;
    if (lang != null) data['languagePreference'] = lang;

    final response = await _dio.put('/users/me', data: data);
    return User.fromJson(response.data['data']);
  }

  Future<List<Report>> getReports({double? lat, double? lng, int? radius, bool? myReports, bool? helping}) async {
    final Map<String, dynamic> query = {};
    if (lat != null && lng != null) {
      query['lat'] = lat;
      query['lng'] = lng;
      query['radius'] = radius ?? 10;
    }
    if (myReports == true) query['myReports'] = true;
    if (helping == true) query['helpingReports'] = true;

    final response = await _dio.get('/reports', queryParameters: query);
    final List list = response.data['data']['content'];
    return list.map((e) => Report.fromJson(e)).toList();
  }

  Future<Report?> getReportDetail(int id) async {
    final response = await _dio.get('/reports/$id');
    return Report.fromJson(response.data['data']);
  }

  Future<bool> createReport(String title, String description, Category category, double lat, double lng, XFile? photo) async {
    FormData formData = FormData.fromMap({
      'title': title,
      'description': description,
      'category': category.toString().split('.').last,
      'latitude': lat,
      'longitude': lng,
      'isAnonymous': false,
    });

    if (photo != null) {
      // --- FIX: WEB SUPPORT FOR FILE UPLOAD ---
      if (kIsWeb) {
        formData.files.add(MapEntry(
          'photo',
          MultipartFile.fromBytes(await photo.readAsBytes(), filename: photo.name),
        ));
      } else {
        formData.files.add(MapEntry(
          'photo',
          await MultipartFile.fromFile(photo.path, filename: photo.name),
        ));
      }
      // ----------------------------------------
    }

    try {
      final response = await _dio.post('/reports', data: formData);
      return response.statusCode == 201;
    } catch (e) {
      return false;
    }
  }

  Future<bool> joinReport(int id) async {
    try {
      final response = await _dio.post('/reports/$id/join');
      return response.data['success'] == true;
    } catch (e) {
      return false;
    }
  }

  Future<List<ReportHistory>> getReportHistory(int id) async {
    final response = await _dio.get('/reports/$id/history');
    final List list = response.data['data'];
    return list.map((e) => ReportHistory.fromJson(e)).toList();
  }

  Future<List<ChatMessage>> getChatMessages(int reportId) async {
    final currentUserId = await getCurrentUserId();
    if (currentUserId == null) return [];

    final response = await _dio.get('/reports/$reportId/chat');
    final List list = response.data['data'];
    return list.map((e) => ChatMessage.fromJson(e, currentUserId)).toList();
  }

  Future<void> sendChatMessageHttp(int reportId, String message) async {
    await _dio.post('/reports/$reportId/chat', data: {'message': message});
  }
}

/* lib/services/api_service.dart
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' hide Category;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:image_picker/image_picker.dart';
import '../models/models.dart';  

class ApiService {
  // Use 10.0.2.2 for Android Emulator, localhost for iOS
  static final String _baseUrl = kIsWeb
      ? 'http://localhost:8080/api'
      : 'http://10.0.2.2:8080/api';

  final Dio _dio = Dio(BaseOptions(baseUrl: _baseUrl));
  final FlutterSecureStorage _storage = const FlutterSecureStorage();
  ApiService._internal() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _storage.read(key: 'jwt_token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
    ));
  }

  // Singleton pattern
  /*static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _storage.read(key: 'jwt_token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
    ));
  }*/

  // --- AUTH ---
  Future<AuthResponse?> login(String email, String password) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'email': email,
        'password': password
      });
      return _handleAuthResponse(response);
    } catch (e) {
      print('Login error: $e');
      return null;
    }
  }

  Future<AuthResponse?> register(String email, String password, String nickname) async {
    try {
      final response = await _dio.post('/auth/register', data: {
        'email': email,
        'password': password,
        'nickname': nickname,
        'isAnonymous': false
      });
      return _handleAuthResponse(response);
    } catch (e) {
      print('Register error: $e');
      return null;
    }
  }

  Future<AuthResponse?> loginAnonymous() async {
    try {
      final response = await _dio.post('/auth/anonymous');
      return _handleAuthResponse(response);
    } catch (e) {
      return null;
    }
  }

  Future<AuthResponse> _handleAuthResponse(Response response) async {
    final data = response.data['data'];
    final authResponse = AuthResponse.fromJson(data);
    await _storage.write(key: 'jwt_token', value: authResponse.token);
    await _storage.write(key: 'user_id', value: authResponse.user.id.toString());
    return authResponse;
  }

  Future<void> logout() async {
    await _storage.deleteAll();
  }

  Future<int?> getCurrentUserId() async {
    final id = await _storage.read(key: 'user_id');
    return id != null ? int.parse(id) : null;
  }

  // --- USER ---
  Future<User?> getUserProfile() async {
    final response = await _dio.get('/users/me');
    return User.fromJson(response.data['data']);
  }

  Future<User?> updateUserProfile({String? nickname, int? radius, String? lang}) async {
    final Map<String, dynamic> data = {};
    if (nickname != null) data['nickname'] = nickname;
    if (radius != null) data['notificationRadius'] = radius;
    if (lang != null) data['languagePreference'] = lang;

    final response = await _dio.put('/users/me', data: data);
    return User.fromJson(response.data['data']);
  }

  // --- REPORTS ---
  Future<List<Report>> getReports({double? lat, double? lng, int? radius, bool? myReports, bool? helping}) async {
    final Map<String, dynamic> query = {};
    if (lat != null && lng != null) {
      query['lat'] = lat;
      query['lng'] = lng;
      query['radius'] = radius ?? 10;
    }
    if (myReports == true) query['myReports'] = true;
    if (helping == true) query['helpingReports'] = true;

    final response = await _dio.get('/reports', queryParameters: query);
    final List list = response.data['data']['content'];
    return list.map((e) => Report.fromJson(e)).toList();
  }

  Future<Report?> getReportDetail(int id) async {
    final response = await _dio.get('/reports/$id');
    return Report.fromJson(response.data['data']);
  }

  Future<bool> createReport(String title, String description, Category category, double lat, double lng, XFile? photo) async {
    FormData formData = FormData.fromMap({
      'title': title,
      'description': description,
      'category': category.toString().split('.').last,
      'latitude': lat,
      'longitude': lng,
      'isAnonymous': false,
    });

    if (photo != null) {
      formData.files.add(MapEntry(
        'photo',
        await MultipartFile.fromFile(photo.path, filename: photo.name),
      ));
    }

    try {
      final response = await _dio.post('/reports', data: formData);
      return response.statusCode == 201;
    } catch (e) {
      return false;
    }
  }

  Future<bool> joinReport(int id) async {
    try {
      final response = await _dio.post('/reports/$id/join');
      return response.data['success'] == true;
    } catch (e) {
      return false;
    }
  }

  Future<List<ReportHistory>> getReportHistory(int id) async {
    final response = await _dio.get('/reports/$id/history');
    final List list = response.data['data'];
    return list.map((e) => ReportHistory.fromJson(e)).toList();
  }

  // --- CHAT ---
  Future<List<ChatMessage>> getChatMessages(int reportId) async {
    final currentUserId = await getCurrentUserId();
    if (currentUserId == null) return [];

    final response = await _dio.get('/reports/$reportId/chat');
    final List list = response.data['data'];
    return list.map((e) => ChatMessage.fromJson(e, currentUserId)).toList();
  }

  Future<void> sendChatMessageHttp(int reportId, String message) async {
    await _dio.post('/reports/$reportId/chat', data: {'message': message});
  }
}*/