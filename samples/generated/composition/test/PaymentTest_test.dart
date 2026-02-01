// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/payment.dart';
import 'package:composition_client/models/bank_transfer_payment.dart';
import 'package:composition_client/models/credit_card_payment.dart';

void main() {
  group('Payment tests', () {
    // oneOf composition tests
    test('fromJson deserializes valid alternative', () {
      // Test with try-each deserialization (no discriminator)
      final json = <String, dynamic>{'cardNumber': 'test_cardNumber'};

      final result = Payment.fromJson(json);

      expect(result, isA<Payment>());
    });

    test('fromJson throws when no alternative matches', () {
      // For try-each oneOf, provide data that won't match any alternative
      final invalidJson = <String, dynamic>{
        '_this_key_will_never_match_any_valid_schema': true,
        '_another_key_that_ensures_no_match': 99999,
        '_yet_another_invalid_field': 'x',
      };

      expect(
        () => Payment.fromJson(invalidJson),
        throwsA(isA<FormatException>()),
      );
    });
  });
}
