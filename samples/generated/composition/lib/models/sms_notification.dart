// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'notification.dart';

part 'sms_notification.g.dart';

/// SmsNotification model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class SmsNotification implements Notification {
  @JsonKey(name: 'phoneNumber')
  final String? phoneNumber;
  @JsonKey(name: 'message')
  final String? message;

  SmsNotification({
    this.phoneNumber,
    this.message,
  });

  factory SmsNotification.fromJson(Map<String, dynamic> json) => _$SmsNotificationFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$SmsNotificationToJson(this);
}
