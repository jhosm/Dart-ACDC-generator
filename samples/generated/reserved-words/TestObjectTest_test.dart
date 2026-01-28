// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:reserved_words_test/models/test_object.dart';

void main() {
  group('TestObject tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'class': 'test_class_',
        'default': 'test_default_',
        'switch': 'test_switch_',
        'return': 42,
        'if': true,
      };

      final model = TestObject.fromJson(json);

      expect(model, isA<TestObject>());
      expect(model.id, isNotNull);
      expect(model.class_, isNotNull);
      expect(model.default_, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'class': 'test_class_',
        'default': 'test_default_',
      };

      final model = TestObject.fromJson(json);

      expect(model, isA<TestObject>());
      expect(model.id, isNotNull);
      expect(model.class_, isNotNull);
      expect(model.default_, isNotNull);
      expect(model.switch_, isNull);
      expect(model.return_, isNull);
      expect(model.if_, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = TestObject(
        id: 'test_id',
        class_: 'test_class_',
        default_: 'test_default_',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('id'), isTrue);
      expect(json.containsKey('class'), isTrue);
      expect(json.containsKey('default'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'id': 'test_id',
        'class': 'test_class_',
        'default': 'test_default_',
        'switch': 'test_switch_',
        'return': 42,
        'if': true,
      };

      final model = TestObject.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['id'], equals(originalJson['id']));
      expect(serializedJson['class'], equals(originalJson['class']));
      expect(serializedJson['default'], equals(originalJson['default']));
    });
  });
}
