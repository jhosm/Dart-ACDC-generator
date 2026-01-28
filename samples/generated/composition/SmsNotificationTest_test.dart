// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/sms_notification.dart';

void main() {
  group('SmsNotification tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'phoneNumber': 'test_phoneNumber',
        'message': 'test_message',
      };

      final model = SmsNotification.fromJson(json);

      expect(model, isA<SmsNotification>());
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
      };

      final model = SmsNotification.fromJson(json);

      expect(model, isA<SmsNotification>());
      expect(model.phoneNumber, isNull);
      expect(model.message, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = SmsNotification(
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'phoneNumber': 'test_phoneNumber',
        'message': 'test_message',
      };

      final model = SmsNotification.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
    });
  });
}
