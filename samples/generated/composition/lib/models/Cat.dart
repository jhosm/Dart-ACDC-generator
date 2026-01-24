// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';
import 'animal.dart';

part 'cat.g.dart';

/// Cat model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Cat extends Animal {
  @JsonKey(name: 'animalType')
  final String animalType;
  @JsonKey(name: 'color')
  final String color;
  @JsonKey(name: 'clawSharpness')
  final int? clawSharpness;

  Cat({
    required this.animalType,
    required this.color,
    this.clawSharpness,
  });

  factory Cat.fromJson(Map<String, dynamic> json) => _$CatFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$CatToJson(this);
}
