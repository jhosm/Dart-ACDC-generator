// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'timestamped.g.dart';

/// Timestamped model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Timestamped {
  @JsonKey(name: 'createdAt')
  final DateTime? createdAt;
  @JsonKey(name: 'updatedAt')
  final DateTime? updatedAt;

  Timestamped({
    this.createdAt,
    this.updatedAt,
  });

  factory Timestamped.fromJson(Map<String, dynamic> json) => _$TimestampedFromJson(json);

  Map<String, dynamic> toJson() => _$TimestampedToJson(this);
}
