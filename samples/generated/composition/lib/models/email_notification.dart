// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'notification.dart';

part 'email_notification.g.dart';

/// EmailNotification model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class EmailNotification extends Notification {
  @JsonKey(name: 'email')
  final String? email;
  @JsonKey(name: 'subject')
  final String? subject;

  EmailNotification({
    this.email,
    this.subject,
  });

  factory EmailNotification.fromJson(Map<String, dynamic> json) => _$EmailNotificationFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$EmailNotificationToJson(this);
}
