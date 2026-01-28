// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:petstore_client/models/new_pet.dart';

void main() {
  group('NewPet tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'name': 'test_name',
        'tag': 'test_tag',
      };

      final model = NewPet.fromJson(json);

      expect(model, isA<NewPet>());
      expect(model.name, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'name': 'test_name',
      };

      final model = NewPet.fromJson(json);

      expect(model, isA<NewPet>());
      expect(model.name, isNotNull);
      expect(model.tag, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = NewPet(
        name: 'test_name',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('name'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'name': 'test_name',
        'tag': 'test_tag',
      };

      final model = NewPet.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['name'], equals(originalJson['name']));
    });
  });
}
