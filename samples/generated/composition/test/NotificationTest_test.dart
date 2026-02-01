// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/notification.dart';
import 'package:composition_client/models/email_notification.dart';
import 'package:composition_client/models/sms_notification.dart';

void main() {
  group('Notification tests', () {
    // anyOf composition tests
    test('fromJson deserializes valid alternative', () {
      final json = <String, dynamic>{};

      final result = Notification.fromJson(json);

      expect(result, isA<Notification>());
    });

    // Note: anyOf with all-optional-fields alternatives cannot reliably test
    // FormatException throwing, as any JSON object will match at least one alternative
  });
}
