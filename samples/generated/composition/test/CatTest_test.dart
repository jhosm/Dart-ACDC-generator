// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/cat.dart';

void main() {
  group('Cat tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'animalType': 'test_animalType',
        'color': 'test_color',
        'clawSharpness': 42,
      };

      final model = Cat.fromJson(json);

      expect(model, isA<Cat>());
      expect(model.animalType, isNotNull);
      expect(model.color, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'animalType': 'test_animalType',
        'color': 'test_color',
      };

      final model = Cat.fromJson(json);

      expect(model, isA<Cat>());
      expect(model.animalType, isNotNull);
      expect(model.color, isNotNull);
      expect(model.clawSharpness, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = Cat(
        animalType: 'test_animalType',
        color: 'test_color',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('animalType'), isTrue);
      expect(json.containsKey('color'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'animalType': 'test_animalType',
        'color': 'test_color',
        'clawSharpness': 42,
      };

      final model = Cat.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['animalType'], equals(originalJson['animalType']));
      expect(serializedJson['color'], equals(originalJson['color']));
    });
  });
}
