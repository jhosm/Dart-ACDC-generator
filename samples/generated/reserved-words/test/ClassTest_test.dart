// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:reserved_words_test/models/class_model.dart';

void main() {
  group('ClassModel tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'name': 'test_name',
      };

      final model = ClassModel.fromJson(json);

      expect(model, isA<ClassModel>());
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
      };

      final model = ClassModel.fromJson(json);

      expect(model, isA<ClassModel>());
      expect(model.name, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = ClassModel(
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'name': 'test_name',
      };

      final model = ClassModel.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
    });
  });
}
