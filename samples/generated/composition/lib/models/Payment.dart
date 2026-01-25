import 'package:composition_client/models/bank_transfer_payment.dart';
import 'package:composition_client/models/credit_card_payment.dart';

/// oneOf composition: Payment
///
/// Generated from OpenAPI oneOf schema.
/// Represents a value that can be one of several alternatives.
abstract class Payment {
  /// Creates an instance from JSON by trying each alternative.
  /// Uses the first alternative that successfully deserializes.
  factory Payment.fromJson(dynamic json) {
    // Try CreditCardPayment
    try {
      return CreditCardPayment.fromJson(json);
    } catch (_) {}
    // Try BankTransferPayment
    try {
      return BankTransferPayment.fromJson(json);
    } catch (_) {}
    throw FormatException('No matching oneOf alternative for Payment');
  }

  Map<String, dynamic> toJson();
}

// Subclass CreditCardPayment is defined in package:composition_client/models/credit_card_payment.dart
// It should extend Payment
// Subclass BankTransferPayment is defined in package:composition_client/models/bank_transfer_payment.dart
// It should extend Payment
