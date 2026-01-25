// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'payment.dart';

part 'bank_transfer_payment.g.dart';

/// BankTransferPayment model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class BankTransferPayment implements Payment {
  @JsonKey(name: 'accountNumber')
  final String accountNumber;
  @JsonKey(name: 'routingNumber')
  final String? routingNumber;

  BankTransferPayment({
    required this.accountNumber,
    this.routingNumber,
  });

  factory BankTransferPayment.fromJson(Map<String, dynamic> json) => _$BankTransferPaymentFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$BankTransferPaymentToJson(this);
}
