// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'package:composition_client/models/animal.dart';

part 'verified_animal.g.dart';

/// VerifiedAnimal model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class VerifiedAnimal {
  @JsonKey(name: 'animalType')
  final String animalType;
  @JsonKey(name: 'breed')
  final String breed;
  @JsonKey(name: 'barkVolume')
  final int? barkVolume;
  @JsonKey(name: 'color')
  final String color;
  @JsonKey(name: 'clawSharpness')
  final int? clawSharpness;
  @JsonKey(name: 'verified')
  final bool? verified;
  @JsonKey(name: 'verifiedAt')
  final DateTime? verifiedAt;

  VerifiedAnimal({
    required this.animalType,
    required this.breed,
    this.barkVolume,
    required this.color,
    this.clawSharpness,
    this.verified,
    this.verifiedAt,
  });

  factory VerifiedAnimal.fromJson(Map<String, dynamic> json) => _$VerifiedAnimalFromJson(json);

  Map<String, dynamic> toJson() => _$VerifiedAnimalToJson(this);
}
