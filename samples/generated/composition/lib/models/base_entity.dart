// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'base_entity.g.dart';

/// BaseEntity model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class BaseEntity {
  @JsonKey(name: 'id')
  final String id;
  @JsonKey(name: 'type')
  final String type;

  BaseEntity({
    required this.id,
    required this.type,
  });

  factory BaseEntity.fromJson(Map<String, dynamic> json) => _$BaseEntityFromJson(json);

  Map<String, dynamic> toJson() => _$BaseEntityToJson(this);
}
