// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'pet.g.dart';

/// Pet model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Pet {
  @JsonKey(name: 'id')
  final int id;
  @JsonKey(name: 'name')
  final String name;
  @JsonKey(name: 'tag')
  final String? tag;
  @JsonKey(name: 'status')
  final String? status;

  Pet({
    required this.id,
    required this.name,
    this.tag,
    this.status,
  });

  factory Pet.fromJson(Map<String, dynamic> json) => _$PetFromJson(json);

  Map<String, dynamic> toJson() => _$PetToJson(this);
}
