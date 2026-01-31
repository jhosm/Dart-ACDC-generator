// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/dog.dart';

void main() {
  group('Dog tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'animalType': 'test_animalType',
        'breed': 'test_breed',
        'barkVolume': 42,
      };

      final model = Dog.fromJson(json);

      expect(model, isA<Dog>());
      expect(model.animalType, isNotNull);
      expect(model.breed, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'animalType': 'test_animalType',
        'breed': 'test_breed',
      };

      final model = Dog.fromJson(json);

      expect(model, isA<Dog>());
      expect(model.animalType, isNotNull);
      expect(model.breed, isNotNull);
      expect(model.barkVolume, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = Dog(
        animalType: 'test_animalType',
        breed: 'test_breed',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('animalType'), isTrue);
      expect(json.containsKey('breed'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'animalType': 'test_animalType',
        'breed': 'test_breed',
        'barkVolume': 42,
      };

      final model = Dog.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['animalType'], equals(originalJson['animalType']));
      expect(serializedJson['breed'], equals(originalJson['breed']));
    });
  });
}
