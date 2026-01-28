// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/verified_animal.dart';
import 'package:composition_client/models/package:composition_client/models/animal.dart';

void main() {
  group('VerifiedAnimal tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'animal': <String, dynamic>{},
        'verified': true,
        'verifiedAt': '2024-01-01T00:00:00.000Z',
      };

      final model = VerifiedAnimal.fromJson(json);

      expect(model, isA<VerifiedAnimal>());
      expect(model.animal, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'animal': <String, dynamic>{},
      };

      final model = VerifiedAnimal.fromJson(json);

      expect(model, isA<VerifiedAnimal>());
      expect(model.animal, isNotNull);
      expect(model.verified, isNull);
      expect(model.verifiedAt, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = VerifiedAnimal(
        animal: null, // TODO: Provide valid test data
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('animal'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'animal': <String, dynamic>{},
        'verified': true,
        'verifiedAt': '2024-01-01T00:00:00.000Z',
      };

      final model = VerifiedAnimal.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['animal'], equals(originalJson['animal']));
    });
  });
}
