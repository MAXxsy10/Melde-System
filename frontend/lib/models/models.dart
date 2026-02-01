// lib/models/models.dart

enum Category { TRASH, DAMAGE, SOCIAL, TRAFFIC, NATURE, OTHER }
enum ReportStatus { NEW, AUTHORITY_NOTIFIED, HELPER_FOUND, IN_PROGRESS, RESOLVED, CLOSED }

class User {
  final int id;
  final String? email;
  final String nickname;
  final bool isAnonymous;
  final int notificationRadius;
  final String languagePreference;

  User({
    required this.id,
    this.email,
    required this.nickname,
    required this.isAnonymous,
    this.notificationRadius = 5,
    this.languagePreference = 'DE',
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      email: json['email'],
      nickname: json['nickname'],
      isAnonymous: json['isAnonymous'] ?? false,
      notificationRadius: json['notificationRadius'] ?? 5,
      languagePreference: json['languagePreference'] ?? 'DE',
    );
  }
}

class LocationDTO {
  final double latitude;
  final double longitude;
  final String? street;
  final String? city;
  final String? postalCode;
  final String? fullAddress;

  LocationDTO({required this.latitude, required this.longitude, this.street, this.city, this.postalCode, this.fullAddress});

  factory LocationDTO.fromJson(Map<String, dynamic> json) {
    return LocationDTO(
      latitude: json['latitude'],
      longitude: json['longitude'],
      street: json['street'],
      city: json['city'],
      postalCode: json['postalCode'],
      fullAddress: json['fullAddress'],
    );
  }
}

class Report {
  final int id;
  final String title;
  final String? description;
  final Category category;
  final String? photoUrl;
  final LocationDTO location;
  final ReportStatus status;
  final DateTime createdAt;
  final int helperCount;
  final bool currentUserIsHelper;
  final bool? currentUserIsCreator; // Available in DetailDTO

  Report({
    required this.id,
    required this.title,
    this.description,
    required this.category,
    this.photoUrl,
    required this.location,
    required this.status,
    required this.createdAt,
    required this.helperCount,
    required this.currentUserIsHelper,
    this.currentUserIsCreator,
  });

  factory Report.fromJson(Map<String, dynamic> json) {
    return Report(
      id: json['id'],
      title: json['title'],
      description: json['description'],
      category: Category.values.firstWhere((e) => e.toString().split('.').last == json['category']),
      photoUrl: json['photoUrl'],
      location: LocationDTO.fromJson(json['location']),
      status: ReportStatus.values.firstWhere((e) => e.toString().split('.').last == json['status']),
      createdAt: DateTime.parse(json['createdAt']),
      helperCount: json['helperCount'] ?? 0,
      currentUserIsHelper: json['currentUserIsHelper'] ?? false,
      currentUserIsCreator: json['currentUserIsCreator'],
    );
  }
}

class ReportHistory {
  final ReportStatus status;
  final String changedBy;
  final DateTime timestamp;
  final String? comment;

  ReportHistory({required this.status, required this.changedBy, required this.timestamp, this.comment});

  factory ReportHistory.fromJson(Map<String, dynamic> json) {
    return ReportHistory(
      status: ReportStatus.values.firstWhere((e) => e.toString().split('.').last == json['status']),
      changedBy: json['changedBy'],
      timestamp: DateTime.parse(json['timestamp']),
      comment: json['comment'],
    );
  }
}

class ChatMessage {
  final int id;
  final int reportId;
  final String senderName;
  final String message;
  final DateTime sentAt;
  final bool isMe; // Helper to determine UI alignment

  ChatMessage({
    required this.id,
    required this.reportId,
    required this.senderName,
    required this.message,
    required this.sentAt,
    this.isMe = false,
  });

  factory ChatMessage.fromJson(Map<String, dynamic> json, int currentUserId) {
    final senderId = json['user']['id'];
    return ChatMessage(
      id: json['id'],
      reportId: json['reportId'],
      senderName: json['user']['nickname'],
      message: json['message'],
      sentAt: DateTime.parse(json['sentAt']),
      isMe: senderId == currentUserId,
    );
  }
}

class AuthResponse {
  final String token;
  final User user;

  AuthResponse({required this.token, required this.user});

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      token: json['token'],
      user: User.fromJson(json['user']),
    );
  }
}