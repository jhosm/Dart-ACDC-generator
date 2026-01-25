
/// oneOf composition: StringOrNumber
///
/// Generated from OpenAPI oneOf schema.
/// Represents a value that can be one of several alternatives.
abstract class StringOrNumber {
  /// Creates an instance from JSON by trying each alternative.
  /// Uses the first alternative that successfully deserializes.
  factory StringOrNumber.fromJson(dynamic json) {
    // Try StringOrNumberString
    try {
      return StringOrNumberString.fromJson(json);
    } catch (_) {}
    // Try StringOrNumberDouble
    try {
      return StringOrNumberDouble.fromJson(json);
    } catch (_) {}
    throw FormatException('No matching oneOf alternative for StringOrNumber');
  }

  Map<String, dynamic> toJson();
}

/// Wrapper class for primitive type String
class StringOrNumberString extends StringOrNumber {
  final String value;

  StringOrNumberString(this.value);

  factory StringOrNumberString.fromJson(dynamic json) {
    if (json is String) {
      return StringOrNumberString(json);
    }
    throw FormatException('Expected String');
  }

  @override
  Map<String, dynamic> toJson() => {'value': value};
}
/// Wrapper class for primitive type double
class StringOrNumberDouble extends StringOrNumber {
  final double value;

  StringOrNumberDouble(this.value);

  factory StringOrNumberDouble.fromJson(dynamic json) {
    if (json is double) {
      return StringOrNumberDouble(json);
    }
    throw FormatException('Expected double');
  }

  @override
  Map<String, dynamic> toJson() => {'value': value};
}
