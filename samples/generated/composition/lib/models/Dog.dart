// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'animal.dart';

part 'dog.g.dart';

/// Dog model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Dog implements Animal {
  @JsonKey(name: 'animalType')
  final String animalType;
  @JsonKey(name: 'breed')
  final String breed;
  @JsonKey(name: 'barkVolume')
  final int? barkVolume;

  Dog({
    required this.animalType,
    required this.breed,
    this.barkVolume,
  });

  factory Dog.fromJson(Map<String, dynamic> json) => _$DogFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$DogToJson(this);
}
