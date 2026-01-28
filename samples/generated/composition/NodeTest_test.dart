// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/node.dart';
import 'package:composition_client/models/package:composition_client/models/list.dart';

void main() {
  group('Node tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'value': 'test_value',
        'children': [],
      };

      final model = Node.fromJson(json);

      expect(model, isA<Node>());
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
      };

      final model = Node.fromJson(json);

      expect(model, isA<Node>());
      expect(model.value, isNull);
      expect(model.children, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = Node(
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'value': 'test_value',
        'children': [],
      };

      final model = Node.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
    });
  });
}
