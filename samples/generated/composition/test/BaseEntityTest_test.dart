// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/base_entity.dart';

void main() {
  group('BaseEntity tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'type': 'test_type',
      };

      final model = BaseEntity.fromJson(json);

      expect(model, isA<BaseEntity>());
      expect(model.id, isNotNull);
      expect(model.type, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'type': 'test_type',
      };

      final model = BaseEntity.fromJson(json);

      expect(model, isA<BaseEntity>());
      expect(model.id, isNotNull);
      expect(model.type, isNotNull);
    });

    test('toJson produces valid JSON', () {
      final model = BaseEntity(
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
      };

      final model = BaseEntity.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['id'], equals(originalJson['id']));
      expect(serializedJson['type'], equals(originalJson['type']));
    });
  });
}
