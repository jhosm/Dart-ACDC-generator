// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'error.g.dart';

/// Error model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Error {
  @JsonKey(name: 'code')
  final int code;
  @JsonKey(name: 'message')
  final String message;

  Error({
    required this.code,
    required this.message,
  });

  factory Error.fromJson(Map<String, dynamic> json) => _$ErrorFromJson(json);

  Map<String, dynamic> toJson() => _$ErrorToJson(this);
}
