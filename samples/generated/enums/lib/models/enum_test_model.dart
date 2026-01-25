// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'enum_test_model.g.dart';

/// EnumTestModel model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class EnumTestModel {
  @JsonKey(name: 'status')
  final String? status;
  @JsonKey(name: 'priority')
  final String? priority;
  @JsonKey(name: 'level')
  final int? level;
  @JsonKey(name: 'category')
  final String? category;
  @JsonKey(name: 'type')
  final String? type;
  @JsonKey(name: 'role')
  final String? role;
  @JsonKey(name: 'emptyTest')
  final String? emptyTest;

  EnumTestModel({
    this.status,
    this.priority,
    this.level,
    this.category,
    this.type,
    this.role,
    this.emptyTest,
  });

  factory EnumTestModel.fromJson(Map<String, dynamic> json) => _$EnumTestModelFromJson(json);

  Map<String, dynamic> toJson() => _$EnumTestModelToJson(this);
}
