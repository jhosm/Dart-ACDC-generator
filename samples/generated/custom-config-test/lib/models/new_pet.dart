// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'new_pet.g.dart';

/// NewPet model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class NewPet {
  @JsonKey(name: 'name')
  final String name;
  @JsonKey(name: 'tag')
  final String? tag;

  NewPet({
    required this.name,
    this.tag,
  });

  factory NewPet.fromJson(Map<String, dynamic> json) => _$NewPetFromJson(json);

  Map<String, dynamic> toJson() => _$NewPetToJson(this);
}
