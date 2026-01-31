# Capability: Generator Core

The foundational OpenAPI Generator infrastructure that enables code generation from OpenAPI specifications.

## ADDED Requirements

### Requirement: Generator Identity

The generator SHALL identify itself as `dart-acdc` when queried for its name, and SHALL provide a help message describing its purpose.

#### Scenario: Generator name returned correctly
- **WHEN** the generator's `getName()` method is called
- **THEN** it SHALL return the string `dart-acdc`

#### Scenario: Generator help message returned
- **WHEN** the generator's `getHelp()` method is called
- **THEN** it SHALL return a description that includes "Dart-ACDC" and "client"

#### Scenario: Generator type is CLIENT
- **WHEN** the generator's `getTag()` method is called
- **THEN** it SHALL return `CodegenType.CLIENT`

### Requirement: Dart Type Mappings

The generator SHALL map OpenAPI types to appropriate Dart types.

#### Scenario: Primitive type mapping
- **WHEN** an OpenAPI schema defines a property with type `integer`
- **THEN** the generated Dart code SHALL use the type `int`

#### Scenario: String type mapping
- **WHEN** an OpenAPI schema defines a property with type `string`
- **THEN** the generated Dart code SHALL use the type `String`

#### Scenario: Boolean type mapping
- **WHEN** an OpenAPI schema defines a property with type `boolean`
- **THEN** the generated Dart code SHALL use the type `bool`

#### Scenario: Number type mapping
- **WHEN** an OpenAPI schema defines a property with type `number` (with any format: `float`, `double`, or no format)
- **THEN** the generated Dart code SHALL use the type `double`

#### Scenario: DateTime type mapping
- **WHEN** an OpenAPI schema defines a property with type `string` and format `date` or `date-time`
- **THEN** the generated Dart code SHALL use the type `DateTime`

#### Scenario: Array type mapping
- **WHEN** an OpenAPI schema defines a property with type `array`
- **THEN** the generated Dart code SHALL use the type `List<T>` where T is the items type

#### Scenario: Object/Map type mapping (additionalProperties only)
- **WHEN** an OpenAPI schema defines a property with type `object` and `additionalProperties` but no `properties`
- **THEN** the generated Dart code SHALL use the type `Map<String, T>` where T is the additionalProperties type (or `dynamic` if unspecified)

#### Scenario: Object type mapping (properties and additionalProperties)
- **WHEN** an OpenAPI schema defines a property with type `object` having both `properties` and `additionalProperties`
- **THEN** the generated Dart code SHALL create a model class with the defined properties
- **AND** SHALL include an `additionalProperties` field of type `Map<String, T>`

#### Scenario: Long integer type mapping
- **WHEN** an OpenAPI schema defines a property with type `integer` and format `int64`
- **THEN** the generated Dart code SHALL use the type `int`

#### Scenario: Binary type mapping
- **WHEN** an OpenAPI schema defines a property with type `string` and format `binary`
- **THEN** the generated Dart code SHALL use the type `List<int>`

#### Scenario: UUID type mapping
- **WHEN** an OpenAPI schema defines a property with type `string` and format `uuid`
- **THEN** the generated Dart code SHALL use the type `String`

#### Scenario: Nullable type mapping
- **WHEN** an OpenAPI schema defines a property with `nullable: true`
- **THEN** the generated Dart code SHALL use a nullable type (e.g., `String?`)

#### Scenario: Enum type mapping
- **WHEN** an OpenAPI schema defines a property with `enum` values
- **THEN** the generated Dart code SHALL create an enum type with the specified values

#### Scenario: Enum value naming
- **WHEN** an OpenAPI schema defines enum values with various formats (e.g., `SOME_VALUE`, `some-value`, `someValue`)
- **THEN** the generated Dart enum SHALL use camelCase for value names (e.g., `someValue`)
- **AND** SHALL use `@JsonValue('SOME_VALUE')` annotation to preserve the original value for serialization

#### Scenario: Enum value collision
- **WHEN** converting enum values to camelCase would result in duplicate names (e.g., `SOME_VALUE` and `some_value` both become `someValue`)
- **THEN** the generator SHALL suffix colliding values with their 1-based index: `someValue`, `someValue2`

#### Scenario: Numeric enum values
- **WHEN** an OpenAPI schema defines enum values that are numeric (e.g., `1`, `2`, `3`)
- **THEN** the generated Dart enum SHALL prefix numeric values with `value` (e.g., `value1`, `value2`)
- **AND** SHALL use `@JsonValue(1)` annotation to preserve the original numeric value

#### Scenario: Model reference mapping
- **WHEN** an OpenAPI schema references another schema via `$ref`
- **THEN** the generated Dart code SHALL use the referenced model type

#### Scenario: File type mapping in multipart context
- **WHEN** an OpenAPI operation's `requestBody.content` contains `multipart/form-data` media type
- **AND** a property has type `string` and format `binary`
- **THEN** the generated Dart code SHALL use the type `MultipartFile` from Dio

#### Scenario: File upload parameter
- **WHEN** an OpenAPI operation defines a request body with `multipart/form-data` containing a file property
- **THEN** the generated Dart method SHALL accept a `MultipartFile` parameter for that property

### Requirement: Schema Composition - allOf

The generator SHALL handle `allOf` composition by merging properties.

#### Scenario: allOf basic composition
- **WHEN** an OpenAPI schema uses `allOf` to combine multiple schemas
- **THEN** the generated Dart class SHALL include all properties from all referenced schemas

#### Scenario: allOf with inline properties
- **WHEN** an OpenAPI schema uses `allOf` combining a `$ref` with additional inline properties
- **THEN** the generated Dart class SHALL include both the referenced schema properties and the inline properties

#### Scenario: allOf property conflict resolution
- **WHEN** an OpenAPI schema uses `allOf` and multiple schemas define the same property name
- **THEN** the generator SHALL use the property definition from the last schema in the allOf array

#### Scenario: allOf property conflict warning
- **WHEN** an OpenAPI schema uses `allOf` and multiple schemas define the same property name with different types
- **THEN** the generator SHALL log a warning message identifying the conflicting property and schemas

#### Scenario: allOf required property merging
- **WHEN** an OpenAPI schema uses `allOf` and schemas have different `required` arrays
- **THEN** the generated Dart class SHALL mark a property as required if ANY schema marks it required

### Requirement: Schema Composition - oneOf

The generator SHALL handle `oneOf` composition using Dart 3 sealed classes.

#### Scenario: oneOf with discriminator
- **WHEN** an OpenAPI schema uses `oneOf` with a `discriminator` property
- **THEN** the generated Dart code SHALL create a sealed base class named after the schema
- **AND** SHALL create subclasses for each alternative prefixed with base name (e.g., `PetCat`, `PetDog`)
- **AND** SHALL use the discriminator property value to select the correct subclass during JSON deserialization

#### Scenario: oneOf without discriminator
- **WHEN** an OpenAPI schema uses `oneOf` without a `discriminator` property
- **THEN** the generated Dart code SHALL create a sealed base class
- **AND** SHALL attempt deserialization of each alternative in array order (first to last as defined in the OpenAPI spec)
- **AND** SHALL use the first alternative that successfully deserializes
- **AND** SHALL throw a `FormatException` if no alternative matches

#### Scenario: oneOf naming convention with $ref
- **WHEN** an OpenAPI schema named `Pet` uses `oneOf` with `$ref` alternatives `Cat` and `Dog`
- **THEN** the generated sealed class SHALL be named `Pet`
- **AND** the subclasses SHALL be prefixed with the base name: `PetCat` and `PetDog`

#### Scenario: oneOf naming convention with inline schemas
- **WHEN** an OpenAPI schema named `Pet` uses `oneOf` with inline schema alternatives (not `$ref`)
- **THEN** the generated sealed class SHALL be named `Pet`
- **AND** inline schema subclasses SHALL be named `{Base}Option{index}` where index is the 1-based position (e.g., `PetOption1`, `PetOption2`)

#### Scenario: oneOf with primitive alternatives
- **WHEN** an OpenAPI schema uses `oneOf` with primitive type alternatives (string, integer, number, boolean)
- **THEN** the generated Dart code SHALL create a sealed class with wrapper subclasses for each primitive
- **AND** each wrapper SHALL be named `{Base}{Type}` (e.g., `PetString`, `PetInt`)
- **AND** each wrapper SHALL contain a `value` property of the primitive type

#### Scenario: oneOf with array or object alternatives
- **WHEN** an OpenAPI schema uses `oneOf` with array or object type alternatives
- **THEN** these SHALL be treated as complex types, not primitives
- **AND** array alternatives SHALL generate a subclass with a `List<T>` property
- **AND** object alternatives SHALL generate a subclass with a `Map<String, dynamic>` property (or typed model if schema is named)

### Requirement: Schema Composition - anyOf

The generator SHALL handle `anyOf` composition similarly to `oneOf`.

#### Scenario: anyOf basic handling
- **WHEN** an OpenAPI schema uses `anyOf` to define flexible alternatives
- **THEN** the generated Dart code SHALL use the same sealed class pattern as `oneOf`
- **AND** deserialization SHALL attempt each alternative in array order (first to last) and use the first successful match

#### Scenario: anyOf documentation
- **WHEN** an OpenAPI schema uses `anyOf`
- **THEN** the generated Dart code SHALL include a doc comment noting that the value may technically match multiple schemas

### Requirement: Nested Composition

The generator SHALL handle nested schema composition.

#### Scenario: allOf containing $ref to oneOf
- **WHEN** an OpenAPI schema uses `allOf` where one element references a `oneOf` schema
- **THEN** the generated Dart class SHALL include a property typed as the sealed class from the oneOf

#### Scenario: Deeply nested composition
- **WHEN** an OpenAPI schema has multiple levels of composition nesting
- **THEN** the generator SHALL resolve each level and generate appropriate Dart types

#### Scenario: Circular reference handling
- **WHEN** an OpenAPI schema contains circular references (e.g., `Person` has property `friend` of type `Person`)
- **THEN** the generator SHALL use nullable types for circular reference properties
- **AND** SHALL use factory constructor pattern for JSON deserialization to handle the cycle

### Requirement: Output Directory Structure

The generator SHALL produce files in a structure that follows Flutter/Dart conventions.

#### Scenario: Models directory exists
- **WHEN** code is generated for an OpenAPI specification with schemas
- **THEN** model files SHALL be placed in the `lib/models/` directory

#### Scenario: Remote data sources directory exists
- **WHEN** code is generated for an OpenAPI specification with paths
- **THEN** API files SHALL be placed in the `lib/remote_data_sources/` directory

#### Scenario: Config directory exists
- **WHEN** code is generated for any OpenAPI specification
- **THEN** the `ApiClient` configuration file SHALL be placed in the `lib/config/` directory

#### Scenario: Barrel export file created
- **WHEN** code is generated for any OpenAPI specification
- **THEN** a barrel export file SHALL be created at `lib/{pubName}.dart`

### Requirement: Build System

The generator SHALL be buildable using Maven with JDK 11 or higher.

#### Scenario: Clean build succeeds
- **WHEN** `mvn clean package -DskipTests` is executed in the generator directory
- **THEN** the command SHALL complete without errors
- **AND** a JAR file SHALL be produced in the `target/` directory

#### Scenario: Generator is discoverable
- **WHEN** the built generator JAR is on the classpath
- **THEN** the `dart-acdc` generator SHALL be discoverable by OpenAPI Generator tools

#### Scenario: JDK version compatibility
- **WHEN** the generator is built with JDK 11 or higher
- **THEN** the build SHALL succeed
- **AND** the generated JAR SHALL be compatible with JDK 11+ runtimes

### Requirement: Generator Options

The generator SHALL support configuration through additional properties.

#### Scenario: Package name option
- **WHEN** the `pubName` additional property is provided
- **THEN** the generated package SHALL use that name in `pubspec.yaml`

#### Scenario: Package version option
- **WHEN** the `pubVersion` additional property is provided
- **THEN** the generated package SHALL use that version in `pubspec.yaml`

#### Scenario: Package description option
- **WHEN** the `pubDescription` additional property is provided
- **THEN** the generated package SHALL use that description in `pubspec.yaml`

#### Scenario: Default options applied
- **WHEN** no additional properties are provided
- **THEN** the generator SHALL use these defaults:
  - `pubName`: derived from OpenAPI `info.title` (sanitized to valid Dart package name), or `openapi_client` if title is missing
  - `pubVersion`: derived from OpenAPI `info.version`, or `1.0.0` if version is missing
  - `pubDescription`: derived from OpenAPI `info.description`, or empty string if description is missing

#### Scenario: Invalid pubName sanitization
- **WHEN** a `pubName` contains invalid Dart package name characters
- **THEN** the generator SHALL sanitize it according to these rules:
  - Convert to lowercase
  - Replace spaces and hyphens with underscores
  - Remove all characters except `a-z`, `0-9`, and `_`
  - Collapse consecutive underscores to single underscore
  - Remove leading/trailing underscores
  - Prefix with `api_` if name starts with a digit
  - Use `openapi_client` if sanitization results in empty string

### Requirement: Reserved Keyword Handling

The generator SHALL handle Dart reserved keywords in generated code by applying consistent escaping rules.

#### Scenario: Reserved keyword in property name
- **WHEN** an OpenAPI schema defines a property named with a Dart reserved keyword (e.g., `class`, `default`, `switch`)
- **THEN** the generated Dart code SHALL suffix the property name with underscore (e.g., `class` → `class_`)
- **AND** SHALL use `@JsonKey(name: 'class')` annotation to preserve JSON serialization

#### Scenario: Reserved keyword in model name
- **WHEN** an OpenAPI schema defines a model named with a Dart reserved keyword (e.g., `Class`, `Switch`)
- **THEN** the generated Dart code SHALL suffix the class name with `Model` (e.g., `Class` → `ClassModel`)

### Requirement: Edge Case Handling

The generator SHALL gracefully handle edge cases in OpenAPI specifications.

#### Scenario: Empty specification
- **WHEN** an OpenAPI specification contains no paths and no schemas
- **THEN** the generator SHALL produce a valid (minimal) Dart package without errors

#### Scenario: Specification with only schemas
- **WHEN** an OpenAPI specification contains schemas but no paths
- **THEN** the generator SHALL produce model files but no remote data source files

#### Scenario: Specification with only paths
- **WHEN** an OpenAPI specification contains paths but no reusable schemas
- **THEN** the generator SHALL produce remote data source files with inline types as needed
