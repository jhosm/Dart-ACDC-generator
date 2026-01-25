// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'upload_response.g.dart';

/// UploadResponse model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class UploadResponse {
  @JsonKey(name: 'id')
  final String id;
  @JsonKey(name: 'filename')
  final String filename;
  @JsonKey(name: 'url')
  final String url;
  @JsonKey(name: 'size')
  final int? size;
  @JsonKey(name: 'mimeType')
  final String? mimeType;

  UploadResponse({
    required this.id,
    required this.filename,
    required this.url,
    this.size,
    this.mimeType,
  });

  factory UploadResponse.fromJson(Map<String, dynamic> json) => _$UploadResponseFromJson(json);

  Map<String, dynamic> toJson() => _$UploadResponseToJson(this);
}
