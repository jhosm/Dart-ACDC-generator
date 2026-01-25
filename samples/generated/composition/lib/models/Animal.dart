import 'package:composition_client/models/cat.dart';
import 'package:composition_client/models/dog.dart';

/// oneOf composition: Animal
///
/// Generated from OpenAPI oneOf schema.
/// Represents a value that can be one of several alternatives.
abstract class Animal {
  /// Creates an instance from JSON using the discriminator property.
  factory Animal.fromJson(Map<String, dynamic> json) {
    final discriminator = json['animalType'];
    switch (discriminator) {
      case 'dog':
        return Dog.fromJson(json);
      case 'cat':
        return Cat.fromJson(json);
      default:
        throw FormatException('Unknown animalType: $discriminator');
    }
  }

  Map<String, dynamic> toJson();
}

// Subclass Dog is defined in package:composition_client/models/dog.dart
// It should extend Animal
// Subclass Cat is defined in package:composition_client/models/cat.dart
// It should extend Animal
