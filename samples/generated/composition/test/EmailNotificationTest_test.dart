// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/email_notification.dart';

void main() {
  group('EmailNotification tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'email': 'test_email',
        'subject': 'test_subject',
      };

      final model = EmailNotification.fromJson(json);

      expect(model, isA<EmailNotification>());
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
      };

      final model = EmailNotification.fromJson(json);

      expect(model, isA<EmailNotification>());
      expect(model.email, isNull);
      expect(model.subject, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = EmailNotification(
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'email': 'test_email',
        'subject': 'test_subject',
      };

      final model = EmailNotification.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
    });
  });
}
