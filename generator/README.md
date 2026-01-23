# Dart-ACDC Generator

A custom OpenAPI Generator that produces Dart API clients with full [Dart-ACDC](https://github.com/jhosm/Dart-ACDC) integration, providing authentication, caching, debugging, and offline support out of the box.

## Features

- **Production-Ready Dart Clients**: Generates Dart API clients following Flutter/Dart best practices
- **Dart-ACDC Integration**: Full integration with Authentication, Caching, Debugging, and Client features
- **Advanced Schema Support**: Handles allOf, oneOf, anyOf compositions, nested schemas, and circular references
- **Type Safety**: Strong Dart type mappings with null safety support
- **Sealed Classes**: Uses Dart 3 sealed classes for discriminated unions (oneOf/anyOf)
- **Smart Naming**: Automatic handling of reserved keywords and naming conventions

## Requirements

### Build Requirements

- **JDK 21+** (Java Development Kit)
- **Maven 3.8+** (Build tool)
- **OpenAPI Generator CLI** (provided in project root as `openapi-generator-cli.jar`)

### Runtime Requirements (for generated code)

- **Dart SDK 3.0+**
- **Flutter 3.10+** (if targeting Flutter applications)

## Version Compatibility

| Component | Version | Notes |
|-----------|---------|-------|
| OpenAPI Generator | 7.10.0 | Pinned version for reproducible builds |
| JDK | 21 | Minimum required version |
| Maven | 3.8+ | Build tool |
| Dart SDK | 3.0+ | For generated client code |
| Flutter | 3.10+ | For Flutter applications |

## Building the Generator

### Quick Build

```bash
# From the generator directory
mvn clean package
```

### Build with Tests

```bash
# Run all unit tests
mvn clean package

# Skip tests for faster builds
mvn clean package -DskipTests
```

### Build Output

The build produces a JAR file at:
```
target/dart-acdc-generator-1.0.0-SNAPSHOT.jar
```

## Usage

### Basic Generation

From the project root directory:

```bash
java -cp "generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar:openapi-generator-cli.jar" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g dart-acdc \
  -i path/to/your/openapi.yaml \
  -o output/directory \
  --additional-properties=pubName=my_api_client
```

### Configuration Options

The generator supports these additional properties:

| Property | Description | Default |
|----------|-------------|---------|
| `pubName` | Dart package name (sanitized automatically) | Derived from OpenAPI `info.title` or `openapi_client` |
| `pubVersion` | Package version | Derived from OpenAPI `info.version` or `1.0.0` |
| `pubDescription` | Package description | Derived from OpenAPI `info.description` or empty |

### Example: Generating from Petstore API

```bash
# Using a sample OpenAPI spec
java -cp "generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar:openapi-generator-cli.jar" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g dart-acdc \
  -i samples/petstore.yaml \
  -o samples/generated/petstore \
  --additional-properties=pubName=petstore_client,pubVersion=1.0.0
```

## Generated Code Structure

The generator produces a Dart package with this structure:

```
output/directory/
├── lib/
│   ├── {pubName}.dart              # Barrel export file
│   ├── api_client.dart             # ApiClient factory
│   ├── config/                     # Configuration classes
│   │   ├── config.dart
│   │   ├── acdc_config.dart
│   │   ├── auth_config.dart
│   │   ├── cache_config.dart
│   │   ├── log_config.dart
│   │   ├── offline_config.dart
│   │   └── security_config.dart
│   ├── models/                     # Data models (generated from schemas)
│   │   └── *.dart
│   └── remote_data_sources/        # API endpoint classes
│       └── *.dart
├── pubspec.yaml                    # Dart package configuration
├── analysis_options.yaml           # Dart analysis configuration
├── .gitignore
└── README.md
```

## Advanced Features

### Schema Composition

The generator handles all OpenAPI composition keywords:

#### allOf (Composition by Merging)

```yaml
# OpenAPI
Entity:
  allOf:
    - $ref: '#/components/schemas/BaseEntity'
    - type: object
      properties:
        name:
          type: string
```

Generates a single Dart class with all properties merged.

#### oneOf (Discriminated Union)

```yaml
# With discriminator
Animal:
  oneOf:
    - $ref: '#/components/schemas/Cat'
    - $ref: '#/components/schemas/Dog'
  discriminator:
    propertyName: animalType
```

Generates a Dart 3 sealed class hierarchy:
```dart
sealed class Animal {
  factory Animal.fromJson(Map<String, dynamic> json) {
    switch (json['animalType']) {
      case 'cat': return AnimalCat.fromJson(json);
      case 'dog': return AnimalDog.fromJson(json);
      // ...
    }
  }
}

class AnimalCat extends Animal { /* ... */ }
class AnimalDog extends Animal { /* ... */ }
```

#### oneOf with Primitives

```yaml
StringOrNumber:
  oneOf:
    - type: string
    - type: number
```

Generates wrapper classes for type safety:
```dart
sealed class StringOrNumber { /* ... */ }

class StringOrNumberString extends StringOrNumber {
  final String value;
  // ...
}

class StringOrNumberDouble extends StringOrNumber {
  final double value;
  // ...
}
```

#### Nested Composition

When allOf references a oneOf/anyOf schema:

```yaml
VerifiedAnimal:
  allOf:
    - $ref: '#/components/schemas/Animal'  # oneOf schema
    - type: object
      properties:
        verified:
          type: boolean
```

Generates a class with a property typed as the sealed class:
```dart
class VerifiedAnimal {
  final Animal animal;      // Sealed class type
  final bool? verified;
  // ...
}
```

#### Circular References

The generator automatically detects and handles circular schema references:

```yaml
Node:
  type: object
  properties:
    value:
      type: string
    children:
      type: array
      items:
        $ref: '#/components/schemas/Node'  # Circular!
```

Circular properties are automatically marked as nullable:
```dart
class Node {
  final String? value;
  final List<Node>? children;  // Nullable due to circular reference
  // ...
}
```

### Reserved Keyword Handling

Dart reserved keywords are automatically escaped:

- **Property names**: Suffixed with underscore (e.g., `class` → `class_`)
- **Model names**: Suffixed with `Model` (e.g., `Class` → `ClassModel`)
- **JSON mapping**: Preserved via `@JsonKey` annotations

### Enum Generation

Enums are generated with collision-resistant naming:

```yaml
Status:
  type: string
  enum:
    - active
    - ACTIVE
    - Active
```

Generates:
```dart
enum Status {
  active,      // First occurrence
  active2,     // Collision resolution
  active3      // Collision resolution
}
```

Numeric enum values are prefixed with `value`:
```dart
enum Priority {
  value1,  // From: 1
  value2,  // From: 2
  value3   // From: 3
}
```

## Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=DartAcdcGeneratorTest

# Run with verbose output
mvn test -X
```

### Test Coverage

The generator includes comprehensive tests for:
- Package name sanitization
- Enum value handling
- Reserved keyword escaping
- Type mappings
- Schema composition (allOf, oneOf, anyOf)
- Circular reference detection
- Nested composition

## Troubleshooting

### Build Issues

#### "Failed to execute goal... compiler error"

**Cause**: JDK version mismatch

**Solution**: Ensure you're using JDK 21 or higher:
```bash
java -version
# Should show: java version "21" or higher
```

Set `JAVA_HOME` if needed:
```bash
export JAVA_HOME=/path/to/jdk-21
```

#### "Dependencies could not be resolved"

**Cause**: Maven unable to download dependencies

**Solution**:
1. Check internet connection
2. Clear Maven cache:
```bash
rm -rf ~/.m2/repository/org/openapitools
mvn clean install
```

### Generation Issues

#### "Error: Could not find or load main class"

**Cause**: Incorrect classpath or missing JAR files

**Solution**: Ensure both JARs are in the classpath:
```bash
# Verify files exist
ls -la generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar
ls -la openapi-generator-cli.jar

# Use quoted classpath
java -cp "generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar:openapi-generator-cli.jar" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g dart-acdc \
  # ...
```

#### "Generator 'dart-acdc' not found"

**Cause**: Generator not properly registered or classpath issue

**Solution**:
1. Rebuild the generator: `mvn clean package`
2. Verify SPI registration file exists:
```bash
jar tf generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar | \
  grep META-INF/services
# Should show: META-INF/services/org.openapitools.codegen.CodegenConfig
```

#### "Invalid OpenAPI specification"

**Cause**: OpenAPI spec validation errors

**Solution**: Validate your spec first:
```bash
java -jar openapi-generator-cli.jar validate -i your-spec.yaml
```

#### Generated code has TODO comments

**Cause**: Template implementation is incomplete for regular object models

**Solution**: This is expected for the current implementation. Only oneOf/anyOf sealed classes and enums generate complete code. Regular object models show TODO placeholders.

### Runtime Issues (Generated Code)

#### "package:dio/dio.dart not found"

**Cause**: Missing Dart-ACDC dependency

**Solution**: Add to generated `pubspec.yaml`:
```yaml
dependencies:
  dart_acdc: ^1.0.0  # Or appropriate version
  dio: ^5.0.0
```

Then run:
```bash
dart pub get
```

## Development

### Project Structure

```
generator/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/openapitools/codegen/languages/
│   │   │       └── DartAcdcGenerator.java
│   │   └── resources/
│   │       ├── dart-acdc/
│   │       │   ├── model.mustache
│   │       │   ├── api.mustache
│   │       │   ├── pubspec.mustache
│   │       │   └── ...
│   │       └── META-INF/services/
│   │           └── org.openapitools.codegen.CodegenConfig
│   └── test/
│       └── java/
│           └── org/openapitools/codegen/languages/
│               └── DartAcdcGeneratorTest.java
├── target/                     # Build output (generated)
├── pom.xml                     # Maven configuration
└── README.md                   # This file
```

### Adding New Features

1. **Modify the generator class**: `src/main/java/.../DartAcdcGenerator.java`
2. **Update templates**: `src/main/resources/dart-acdc/*.mustache`
3. **Add tests**: `src/test/java/.../DartAcdcGeneratorTest.java`
4. **Rebuild**: `mvn clean package`
5. **Test**: Generate sample code and verify output

### Template Variables

Common Mustache variables available in templates:

- `{{classname}}` - Model class name
- `{{description}}` - Model description
- `{{vars}}` - List of properties
- `{{imports}}` - Required imports
- `{{vendorExtensions.x-*}}` - Custom metadata

See existing templates in `src/main/resources/dart-acdc/` for examples.

## Contributing

See the main project documentation for contribution guidelines.

## Resources

- **Dart-ACDC Library**: https://github.com/jhosm/Dart-ACDC
- **OpenAPI Generator**: https://openapi-generator.tech/
- **OpenAPI Specification**: https://swagger.io/specification/
- **Dart Language**: https://dart.dev/
- **Flutter**: https://flutter.dev/

## License

[Add license information here]
