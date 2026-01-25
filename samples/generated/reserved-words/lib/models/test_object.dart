// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'test_object.g.dart';

/// TestObject model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class TestObject {
  /// Object identifier
  @JsonKey(name: 'id')
  final String id;
  /// Property named 'class' (reserved keyword)
  @JsonKey(name: 'class')
  final String class_;
  /// Property named 'default' (reserved keyword)
  @JsonKey(name: 'default')
  final String default_;
  /// Property named 'switch' (reserved keyword)
  @JsonKey(name: 'switch')
  final String? switch_;
  /// Property named 'return' (reserved keyword)
  @JsonKey(name: 'return')
  final int? return_;
  /// Property named 'if' (reserved keyword)
  @JsonKey(name: 'if')
  final bool? if_;

  TestObject({
    required this.id,
    required this.class_,
    required this.default_,
    this.switch_,
    this.return_,
    this.if_,
  });

  factory TestObject.fromJson(Map<String, dynamic> json) => _$TestObjectFromJson(json);

  Map<String, dynamic> toJson() => _$TestObjectToJson(this);
}
