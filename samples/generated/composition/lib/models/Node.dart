// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'node.g.dart';

/// Node model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Node {
  @JsonKey(name: 'value')
  final String? value;
  @JsonKey(name: 'children')
  final List? children;

  Node({
    this.value,
    this.children,
  });

  factory Node.fromJson(Map<String, dynamic> json) => _$NodeFromJson(json);

  Map<String, dynamic> toJson() => _$NodeToJson(this);
}
