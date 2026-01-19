# Design: Bootstrap Generator

## Context

OpenAPI Generator can be extended in two ways:
1. **Fork & modify** - Clone the full repository, add generator, build everything
2. **Standalone generator** - Create an independent Maven project that depends on `openapi-generator-core`

We choose the **standalone generator** approach for this project.

## Goals

- Create a self-contained generator that can be built independently
- Follow OpenAPI Generator conventions for future contribution compatibility
- Enable rapid development iteration without full OpenAPI Generator builds
- Support both JAR distribution and potential contribution back to main repository

## Naming Convention

**Important distinction**:
- **Project name**: `dart-acdc-generator` (repository, documentation, package)
- **Generator name**: `dart-acdc` (what OpenAPI Generator CLI uses: `-g dart-acdc`)
- **Java class**: `DartAcdcGenerator` (follows OpenAPI Generator convention)

This follows OpenAPI Generator conventions where generator names are short (e.g., `dart-dio`, `typescript-axios`) while the containing project/package may have a longer descriptive name.

## Non-Goals

- Full integration into OpenAPI Generator repository (future Phase 5)
- Docker/NPM packaging (future Phase 5)
- Comprehensive testing framework (future Phase 3)

## Decisions

### Decision 1: Standalone Generator Architecture

**What**: Create an independent Maven project under `generator/` directory.

**Why**:
- Faster build times (no need to compile entire OpenAPI Generator)
- Easier development iteration
- Can be migrated to main repository later
- Users can build and use without cloning OpenAPI Generator

**Alternatives considered**:
- Fork OpenAPI Generator: Rejected - too heavyweight for initial development
- Gradle instead of Maven: Rejected - OpenAPI Generator uses Maven, stay compatible

### Decision 2: Extend DefaultCodegen (not DartDioClientCodegen)

**What**: `DartAcdcGenerator extends DefaultCodegen implements CodegenConfig`

**Why**:
- Clean slate for ACDC-specific patterns
- Full control over template structure
- [ADR-001](../../../research/adr-001-generated-code-architecture.md) defines different naming conventions (`*RemoteDataSource`) than dart-dio
- Avoid inheriting patterns that conflict with our architecture

**Alternatives considered**:
- Extend `DartDioClientCodegen`: Rejected - would inherit conventions we want to change
- Extend `AbstractDartCodegen`: Considered - might revisit for shared Dart utilities

### Decision 3: Generated Code Structure

**What**: Follow ADR-001 folder structure:
```
lib/
├── {pubName}.dart               # Barrel export
├── api_client.dart              # ApiClient factory
├── config/                      # Configuration classes
├── models/                      # Data models
└── remote_data_sources/         # API classes (interface + impl)
```

**Why**:
- Matches Flutter/Dart clean architecture conventions
- Supports testability (interface/implementation split)
- Clear separation of concerns

### Decision 4: OpenAPI Generator Version Pinning

**What**: Pin to OpenAPI Generator 7.10.0 (latest stable as of January 2025).

**Why**:
- Ensures reproducible builds
- Avoids breaking changes from upstream updates
- Allows controlled upgrades with testing

**Version strategy**:
- Use Maven dependency with exact version: `7.10.0`
- Document compatibility in README
- Test before upgrading to newer versions
- Update version only through explicit proposal/PR with testing

### Decision 5: Reserved Keyword Handling

**What**: Suffix Dart reserved keywords with underscore when used as property names, suffix with `Model` when used as class names.

**Why**:
- Dart has ~60 reserved keywords that cannot be used as identifiers
- OpenAPI specs may use names like `class`, `default`, `switch` for properties
- Must generate valid Dart code
- Suffix convention chosen over prefix for readability (`class_` reads better than `_class`)

**Strategy**:
- Property names: Suffix with underscore (e.g., `class` → `class_`), use `@JsonKey(name: 'class')` for correct JSON serialization
- Model names: Suffix with `Model` (e.g., `Class` → `ClassModel`)

### Decision 6: Schema Composition Strategy

**What**: Handle `allOf`, `oneOf`, and `anyOf` OpenAPI composition keywords.

**Why**:
- Schema composition is common in real-world OpenAPI specs
- Enables code reuse and polymorphism in API definitions
- Must generate idiomatic Dart code

**Strategy**:

**allOf** (intersection/merge):
- Flatten all properties into a single class
- If combining `$ref` with inline properties, merge them
- Property conflict resolution: last definition wins, log warning
- Required merging: property is required if ANY schema marks it required
- Example: `allOf: [{$ref: '#/components/schemas/Base'}, {properties: {extra: {type: string}}}]` → Single class with all properties

**oneOf** (discriminated union):
- Generate a Dart 3 sealed class hierarchy
- Naming: Base class = schema name, Subclasses always prefixed with base name: `{Base}{Alternative}` (e.g., `Pet` → `PetCat`, `PetDog`)
- Inline schema naming: Use `{Base}Option{index}` where index is 1-based position in oneOf array (e.g., `PetOption1`, `PetOption2`)
- With discriminator: Use discriminator property value to select subclass during deserialization
- Without discriminator: Try each alternative in array order (first to last as defined in OpenAPI spec), use first successful parse
- Primitive alternatives: Wrap primitives (string, integer, number, boolean) in value classes (e.g., `PetString`, `PetInt`). Arrays and objects are not considered primitives.

Example with discriminator:
```dart
sealed class Pet {
  factory Pet.fromJson(Map<String, dynamic> json) {
    switch (json['petType']) {
      case 'cat': return PetCat.fromJson(json);
      case 'dog': return PetDog.fromJson(json);
      default: throw FormatException('Unknown petType: ${json['petType']}');
    }
  }
}

class PetCat extends Pet { ... }
class PetDog extends Pet { ... }
```

Example without discriminator:
```dart
sealed class Pet {
  factory Pet.fromJson(Map<String, dynamic> json) {
    // Try each alternative in order
    try { return PetCat.fromJson(json); } catch (_) {}
    try { return PetDog.fromJson(json); } catch (_) {}
    throw FormatException('No matching oneOf alternative for Pet');
  }
}
```

Example with primitives:
```dart
sealed class StringOrInt {
  factory StringOrInt.fromJson(dynamic json) {
    if (json is String) return StringOrIntString(json);
    if (json is int) return StringOrIntInt(json);
    throw FormatException('Expected String or int');
  }
}

class StringOrIntString extends StringOrInt {
  final String value;
  StringOrIntString(this.value);
}

class StringOrIntInt extends StringOrInt {
  final int value;
  StringOrIntInt(this.value);
}
```

**anyOf** (flexible union):
- Same implementation as oneOf (Dart can't distinguish at runtime)
- Add doc comment noting semantic difference
- Deserialization attempts each alternative in array order (first to last), uses first successful match

**Nested composition**:
- allOf containing oneOf: Property typed as sealed class
- Resolve recursively, generate types bottom-up
- Circular references: Use nullable types with factory constructor pattern (Dart has no forward declarations)

**Alternatives considered**:
- Use `dynamic` for oneOf/anyOf: Rejected - loses type safety
- Use inheritance for allOf: Rejected - Dart single inheritance limitation
- Use mixins for allOf: Considered - may revisit for complex hierarchies
- Use freezed unions: Considered - adds dependency, may offer as option later

### Decision 7: File Upload Handling

**What**: Map file uploads to Dio's `MultipartFile` type.

**Why**:
- File uploads are common in REST APIs
- Dio provides `MultipartFile` for multipart/form-data requests
- Must integrate with Dart-ACDC's Dio-based client

**Strategy**:
- `type: string, format: binary` in multipart context → `MultipartFile`
  - Multipart context defined as: operation's `requestBody.content` contains `multipart/form-data` media type
- `type: string, format: binary` in non-multipart context → `List<int>` (raw bytes)
  - Non-multipart context: all other cases (response bodies, `application/octet-stream`, etc.)
- Generate helper methods for file upload endpoints using `FormData`

**Example generated code**:
```dart
Future<UploadResponse> uploadFile(
  String userId,
  MultipartFile file, {
  String? description,
}) async {
  final formData = FormData.fromMap({
    'file': file,
    if (description != null) 'description': description,
  });
  final response = await _dio.post('/users/$userId/upload', data: formData);
  return UploadResponse.fromJson(response.data);
}
```

**Dependencies**:
- Requires `dio` package (already included via Dart-ACDC)

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Standalone generator may diverge from OpenAPI Generator updates | Pin to specific version, document compatibility |
| Maven setup complexity | Provide clear documentation and build scripts |
| JDK version conflicts | Document required JDK version (11+), test on common versions |
| Reserved keyword collisions | Implement consistent escaping strategy with JsonKey annotations |
| Edge case OpenAPI specs | Test with diverse specs including empty, minimal, and complex examples |
| Complex oneOf/anyOf hierarchies | Use Dart 3 sealed classes, test with real-world polymorphic APIs |
| allOf property conflicts | Document merge strategy, prefer more specific definition |
| File upload edge cases | Test with various file types, multiple files, and mixed form data |

## Migration Plan

Not applicable - this is the initial setup.

## Open Questions

1. **Should we support Gradle as an alternative build system?**
   - Current decision: No, stick with Maven for OpenAPI Generator compatibility
   - Revisit in Phase 5 (Distribution)

2. **Should we include OpenAPI Generator as a git submodule?**
   - Current decision: No, use Maven dependency
   - Benefit: Cleaner repository, easier updates
   - Trade-off: Cannot easily reference generator source code
