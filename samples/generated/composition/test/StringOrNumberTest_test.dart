// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/string_or_number.dart';

void main() {
  group('StringOrNumber tests', () {
    // oneOf composition tests
    test('fromJson deserializes valid alternative', () {
      // Test with try-each deserialization (no discriminator)
      final json = 'test_value';

      final result = StringOrNumber.fromJson(json);

      expect(result, isA<StringOrNumber>());
    });

    test('fromJson throws when no alternative matches', () {
      // For try-each oneOf, provide data that won't match any alternative
      final invalidJson = <String, dynamic>{
        '_this_key_will_never_match_any_valid_schema': true,
        '_another_key_that_ensures_no_match': 99999,
        '_yet_another_invalid_field': 'x',
      };

      expect(
        () => StringOrNumber.fromJson(invalidJson),
        throwsA(isA<FormatException>()),
      );
    });
  });
}
