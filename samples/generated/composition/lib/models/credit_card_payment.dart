// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'payment.dart';

part 'credit_card_payment.g.dart';

/// CreditCardPayment model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class CreditCardPayment extends Payment {
  @JsonKey(name: 'cardNumber')
  final String cardNumber;
  @JsonKey(name: 'cvv')
  final String? cvv;

  CreditCardPayment({
    required this.cardNumber,
    this.cvv,
  });

  factory CreditCardPayment.fromJson(Map<String, dynamic> json) => _$CreditCardPaymentFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$CreditCardPaymentToJson(this);
}
