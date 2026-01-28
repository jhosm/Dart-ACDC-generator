// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:petstore_client/models/error.dart';

void main() {
  group('Error tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'code': 42,
        'message': 'test_message',
      };

      final model = Error.fromJson(json);

      expect(model, isA<Error>());
      expect(model.code, isNotNull);
      expect(model.message, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'code': 42,
        'message': 'test_message',
      };

      final model = Error.fromJson(json);

      expect(model, isA<Error>());
      expect(model.code, isNotNull);
      expect(model.message, isNotNull);
    });

    test('toJson produces valid JSON', () {
      final model = Error(
        code: 42,
        message: 'test_message',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('code'), isTrue);
      expect(json.containsKey('message'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'code': 42,
        'message': 'test_message',
      };

      final model = Error.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['code'], equals(originalJson['code']));
      expect(serializedJson['message'], equals(originalJson['message']));
    });
  });
}
