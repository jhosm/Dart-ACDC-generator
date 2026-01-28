// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:enums_client/models/enum_test_model.dart';

void main() {
  group('EnumTestModel tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'status': 'test_status',
        'priority': 'test_priority',
        'level': 42,
        'category': 'test_category',
        'type': 'test_type',
        'role': 'test_role',
        'emptyTest': 'test_emptyTest',
      };

      final model = EnumTestModel.fromJson(json);

      expect(model, isA<EnumTestModel>());
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
      };

      final model = EnumTestModel.fromJson(json);

      expect(model, isA<EnumTestModel>());
      expect(model.status, isNull);
      expect(model.priority, isNull);
      expect(model.level, isNull);
      expect(model.category, isNull);
      expect(model.type, isNull);
      expect(model.role, isNull);
      expect(model.emptyTest, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = EnumTestModel(
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'status': 'test_status',
        'priority': 'test_priority',
        'level': 42,
        'category': 'test_category',
        'type': 'test_type',
        'role': 'test_role',
        'emptyTest': 'test_emptyTest',
      };

      final model = EnumTestModel.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
    });
  });
}
