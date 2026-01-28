// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/timestamped.dart';

void main() {
  group('Timestamped tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'createdAt': '2024-01-01T00:00:00.000Z',
        'updatedAt': '2024-01-01T00:00:00.000Z',
      };

      final model = Timestamped.fromJson(json);

      expect(model, isA<Timestamped>());
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
      };

      final model = Timestamped.fromJson(json);

      expect(model, isA<Timestamped>());
      expect(model.createdAt, isNull);
      expect(model.updatedAt, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = Timestamped(
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'createdAt': '2024-01-01T00:00:00.000Z',
        'updatedAt': '2024-01-01T00:00:00.000Z',
      };

      final model = Timestamped.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
    });
  });
}
