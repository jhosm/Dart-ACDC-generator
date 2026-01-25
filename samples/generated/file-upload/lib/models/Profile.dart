// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:json_annotation/json_annotation.dart';

part 'profile.g.dart';

/// Profile model
///
/// Generated from OpenAPI schema.
@JsonSerializable()
class Profile {
  @JsonKey(name: 'id')
  final String id;
  @JsonKey(name: 'name')
  final String name;
  @JsonKey(name: 'bio')
  final String? bio;
  @JsonKey(name: 'avatarUrl')
  final String avatarUrl;

  Profile({
    required this.id,
    required this.name,
    this.bio,
    required this.avatarUrl,
  });

  factory Profile.fromJson(Map<String, dynamic> json) => _$ProfileFromJson(json);

  Map<String, dynamic> toJson() => _$ProfileToJson(this);
}
