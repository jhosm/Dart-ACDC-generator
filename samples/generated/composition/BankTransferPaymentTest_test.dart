// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:composition_client/models/bank_transfer_payment.dart';

void main() {
  group('BankTransferPayment tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'accountNumber': 'test_accountNumber',
        'routingNumber': 'test_routingNumber',
      };

      final model = BankTransferPayment.fromJson(json);

      expect(model, isA<BankTransferPayment>());
      expect(model.accountNumber, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'accountNumber': 'test_accountNumber',
      };

      final model = BankTransferPayment.fromJson(json);

      expect(model, isA<BankTransferPayment>());
      expect(model.accountNumber, isNotNull);
      expect(model.routingNumber, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = BankTransferPayment(
        accountNumber: 'test_accountNumber',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('accountNumber'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'accountNumber': 'test_accountNumber',
        'routingNumber': 'test_routingNumber',
      };

      final model = BankTransferPayment.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['accountNumber'], equals(originalJson['accountNumber']));
    });
  });
}
