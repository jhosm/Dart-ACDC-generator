// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/credit_card_payment.dart';

void main() {
  group('CreditCardPayment tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'cardNumber': 'test_cardNumber',
        'cvv': 'test_cvv',
      };

      final model = CreditCardPayment.fromJson(json);

      expect(model, isA<CreditCardPayment>());
      expect(model.cardNumber, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'cardNumber': 'test_cardNumber',
      };

      final model = CreditCardPayment.fromJson(json);

      expect(model, isA<CreditCardPayment>());
      expect(model.cardNumber, isNotNull);
      expect(model.cvv, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = CreditCardPayment(
        cardNumber: 'test_cardNumber',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('cardNumber'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'cardNumber': 'test_cardNumber',
        'cvv': 'test_cvv',
      };

      final model = CreditCardPayment.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['cardNumber'], equals(originalJson['cardNumber']));
    });
  });
}
