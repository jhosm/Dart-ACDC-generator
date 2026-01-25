// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'ping200_response.g.dart';

/// Ping200Response model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Ping200Response {
  @JsonKey(name: 'status')
  final String? status;

  Ping200Response({
    this.status,
  });

  factory Ping200Response.fromJson(Map<String, dynamic> json) => _$Ping200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$Ping200ResponseToJson(this);
}
