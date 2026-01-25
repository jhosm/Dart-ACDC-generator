import 'package:composition_client/models/email_notification.dart';
import 'package:composition_client/models/sms_notification.dart';

/// anyOf composition: Notification
///
/// Generated from OpenAPI anyOf schema.
/// Represents a value that can match any of several alternatives.
/// Note: At runtime, Dart cannot distinguish between oneOf and anyOf semantics.
/// The first matching alternative will be used during deserialization.
abstract class Notification {
  /// Creates an instance from JSON by trying each alternative.
  /// Uses the first alternative that successfully deserializes.
  factory Notification.fromJson(dynamic json) {
    // Try EmailNotification
    try {
      return EmailNotification.fromJson(json);
    } catch (_) {}
    // Try SmsNotification
    try {
      return SmsNotification.fromJson(json);
    } catch (_) {}
    throw FormatException('No matching anyOf alternative for Notification');
  }

  Map<String, dynamic> toJson();
}

// Subclass EmailNotification is defined in package:composition_client/models/email_notification.dart
// It should extend Notification
// Subclass SmsNotification is defined in package:composition_client/models/sms_notification.dart
// It should extend Notification
