// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'class_model.g.dart';

/// Model named 'Class' (reserved keyword)
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class ClassModel {
  @JsonKey(name: 'name')
  final String? name;

  ClassModel({
    this.name,
  });

  factory ClassModel.fromJson(Map<String, dynamic> json) => _$ClassModelFromJson(json);

  Map<String, dynamic> toJson() => _$ClassModelToJson(this);
}
