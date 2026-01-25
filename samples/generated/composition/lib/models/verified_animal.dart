// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'package:composition_client/models/animal.dart';

part 'verified_animal.g.dart';

/// VerifiedAnimal model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class VerifiedAnimal {
  @JsonKey(name: 'animal', fromJson: _animalFromJson, toJson: _animalToJson)
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

  /// Custom JSON converter for oneOf/anyOf property: animal
  static Animal _animalFromJson(Map<String, dynamic> json) {
    return Animal.fromJson(json);
  }

  static Map<String, dynamic> _animalToJson(Animal value) {
    return value.toJson();
  }

  factory VerifiedAnimal.fromJson(Map<String, dynamic> json) => _$VerifiedAnimalFromJson(json);

  Map<String, dynamic> toJson() => _$VerifiedAnimalToJson(this);
}
