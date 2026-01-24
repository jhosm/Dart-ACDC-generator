// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'entity.g.dart';

/// Entity model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Entity {
  @JsonKey(name: 'id')
  final String id;
  @JsonKey(name: 'type')
  final String type;
  @JsonKey(name: 'createdAt')
  final DateTime? createdAt;
  @JsonKey(name: 'updatedAt')
  final DateTime? updatedAt;
  @JsonKey(name: 'name')
  final String? name;

  Entity({
    required this.id,
    required this.type,
    this.createdAt,
    this.updatedAt,
    this.name,
  });

  factory Entity.fromJson(Map<String, dynamic> json) => _$EntityFromJson(json);

  Map<String, dynamic> toJson() => _$EntityToJson(this);
}
