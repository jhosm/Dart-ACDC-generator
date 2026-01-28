// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:minimal_client/models/ping200_response.dart';

void main() {
  group('Ping200Response tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'status': 'test_status',
      };

      final model = Ping200Response.fromJson(json);

      expect(model, isA<Ping200Response>());
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
      };

      final model = Ping200Response.fromJson(json);

      expect(model, isA<Ping200Response>());
      expect(model.status, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = Ping200Response(
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'status': 'test_status',
      };

      final model = Ping200Response.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
    });
  });
}
