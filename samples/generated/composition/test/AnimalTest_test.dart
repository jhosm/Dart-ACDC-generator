// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/animal.dart';
import 'package:composition_client/models/cat.dart';
import 'package:composition_client/models/dog.dart';

void main() {
  group('Animal tests', () {
    // oneOf composition tests
    test('fromJson deserializes valid alternative', () {
      // Test with discriminator-based deserialization
      final json = <String, dynamic>{'animalType': 'test_animalType', 'breed': 'test_breed'};
      json['animalType'] = 'dog';

      final result = Animal.fromJson(json);

      expect(result, isA<Animal>());
      expect(result, isA<Dog>());
    });

    test('fromJson throws on invalid discriminator', () {
      final json = <String, dynamic>{'animalType': 'test_animalType', 'breed': 'test_breed'};
      json['animalType'] = '_invalid_discriminator_value_';

      expect(
        () => Animal.fromJson(json),
        throwsA(isA<FormatException>()),
      );
    });
  });
}
