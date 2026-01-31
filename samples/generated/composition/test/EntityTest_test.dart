// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/entity.dart';

void main() {
  group('Entity tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'type': 'test_type',
        'createdAt': '2024-01-01T00:00:00.000Z',
        'updatedAt': '2024-01-01T00:00:00.000Z',
        'name': 'test_name',
      };

      final model = Entity.fromJson(json);

      expect(model, isA<Entity>());
      expect(model.id, isNotNull);
      expect(model.type, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'type': 'test_type',
      };

      final model = Entity.fromJson(json);

      expect(model, isA<Entity>());
      expect(model.id, isNotNull);
      expect(model.type, isNotNull);
      expect(model.createdAt, isNull);
      expect(model.updatedAt, isNull);
      expect(model.name, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = Entity(
        id: 'test_id',
        type: 'test_type',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('id'), isTrue);
      expect(json.containsKey('type'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'id': 'test_id',
        'type': 'test_type',
        'createdAt': '2024-01-01T00:00:00.000Z',
        'updatedAt': '2024-01-01T00:00:00.000Z',
        'name': 'test_name',
      };

      final model = Entity.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['id'], equals(originalJson['id']));
      expect(serializedJson['type'], equals(originalJson['type']));
    });
  });
}
