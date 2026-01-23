/// oneOf composition: Payment
///
/// Generated from OpenAPI oneOf schema.
/// Represents a value that can be one of several alternatives.
sealed class Payment {
  /// Creates an instance from JSON by trying each alternative.
  /// Uses the first alternative that successfully deserializes.
  factory Payment.fromJson(dynamic json) {
    // Try PaymentCreditCardPayment
    try {
      return PaymentCreditCardPayment.fromJson(json);
    } catch (_) {}
    // Try PaymentBankTransferPayment
    try {
      return PaymentBankTransferPayment.fromJson(json);
    } catch (_) {}
    throw FormatException('No matching oneOf alternative for Payment');
  }

  Map<String, dynamic> toJson();
}

// Subclass PaymentCreditCardPayment is defined in package:composition_client/models/credit_card_payment.dart
// It should extend Payment
// Subclass PaymentBankTransferPayment is defined in package:composition_client/models/bank_transfer_payment.dart
// It should extend Payment
