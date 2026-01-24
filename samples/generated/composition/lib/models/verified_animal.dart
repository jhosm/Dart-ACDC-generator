// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'verified_animal.g.dart';

/// VerifiedAnimal model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class VerifiedAnimal {
  @JsonKey(name: 'animal')
  final Animal animal;
  @JsonKey(name: 'verified')
  final bool? verified;
  @JsonKey(name: 'verifiedAt')
  final DateTime? verifiedAt;

  VerifiedAnimal({
    required this.animal,
    this.verified,
    this.verifiedAt,
  });

  factory VerifiedAnimal.fromJson(Map<String, dynamic> json) => _$VerifiedAnimalFromJson(json);

  Map<String, dynamic> toJson() => _$VerifiedAnimalToJson(this);
}
