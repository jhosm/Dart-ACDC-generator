// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:petstore_client/models/pet.dart';

void main() {
  group('Pet tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'id': 42,
        'name': 'test_name',
        'tag': 'test_tag',
        'status': 'test_status',
      };

      final model = Pet.fromJson(json);

      expect(model, isA<Pet>());
      expect(model.id, isNotNull);
      expect(model.name, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'id': 42,
        'name': 'test_name',
      };

      final model = Pet.fromJson(json);

      expect(model, isA<Pet>());
      expect(model.id, isNotNull);
      expect(model.name, isNotNull);
      expect(model.tag, isNull);
      expect(model.status, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = Pet(
        id: 42,
        name: 'test_name',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('id'), isTrue);
      expect(json.containsKey('name'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'id': 42,
        'name': 'test_name',
        'tag': 'test_tag',
        'status': 'test_status',
      };

      final model = Pet.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['id'], equals(originalJson['id']));
      expect(serializedJson['name'], equals(originalJson['name']));
    });
  });
}
