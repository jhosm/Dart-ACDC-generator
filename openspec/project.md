# Project Context

## Purpose

**Dart-ACDC-generator** is a custom OpenAPI Generator that automatically generates Dart API clients fully integrated with the Dart-ACDC library. The goal is to eliminate manual integration work by producing production-ready clients with authentication, caching, offline support, and logging built-in.

### Problem
OpenAPI Generator creates basic API clients, but production Flutter apps need authentication, caching, offline support, and logging—features provided by Dart-ACDC but requiring manual integration.

### Solution
A custom OpenAPI Generator that produces production-ready Dart clients with Dart-ACDC fully integrated out of the box.

## Tech Stack

- **Generator Language**: Java 11+
- **Build System**: Maven
- **Templating**: Mustache
- **Generated Code Language**: Dart 3.0+
- **Generated Code Dependencies**:
  - `dart_acdc` - Production HTTP client with ACDC features
  - `dio` - HTTP client (via Dart-ACDC)
  - `json_annotation` / `json_serializable` - JSON serialization
- **Target Platform**: Flutter (mobile, desktop, web)

## Project Conventions

### Code Style

**Java (Generator)**:
- Follow OpenAPI Generator coding conventions
- Use meaningful names for methods and variables
- Document public APIs with Javadoc

**Mustache Templates**:
- One template per generated file type
- Use conditional blocks for optional features
- Preserve indentation for readable output

**Generated Dart Code**:
- Follow Dart effective style guide
- Use camelCase for methods/variables
- Use PascalCase for classes
- Use snake_case for file names

### Architecture Patterns

**Naming Convention**: `*RemoteDataSource` for API classes (ADR-001)
- `UserRemoteDataSource` (interface)
- `UserRemoteDataSourceImpl` (implementation)

**Interface/Implementation Split**: Generate both abstract interface and concrete implementation for testability.

**Error Handling**: Wrap `DioException` → `AcdcException` in all API methods.

**Dependency Injection**: Constructor injection of `Dio` instance.

**Configuration**: Type-safe configuration classes for each ACDC pillar.

### Testing Strategy

- Use Petstore OpenAPI spec for integration testing
- Run `dart analyze` on all generated code
- Run `dart pub get` and `dart run build_runner build`
- Manual review of generated code quality

### Git Workflow

- Feature branches for each proposal
- PR-based code review
- Conventional commits (feat:, fix:, docs:, etc.)
- Squash merge to main

## Domain Context

### The Four Pillars of ACDC

1. **Authentication (A)**: OAuth 2.1, automatic token refresh, secure storage
2. **Caching (C)**: Two-tier (Memory + Disk), user isolation, encryption
3. **Debugging (D)**: Configurable logging, sensitive data redaction
4. **Client (C)**: Request deduplication, certificate pinning, offline support

### OpenAPI Generator Concepts

- **Codegen Class**: Java class that extends `DefaultCodegen`, configures type mappings and options
- **Mustache Templates**: Template files that generate source code
- **SPI Registration**: Service provider interface for generator discovery
- **Supporting Files**: Static files copied to output (README, pubspec, etc.)

## Important Constraints

- Must be compatible with OpenAPI Generator 7.x+
- Generated code must support Dart 3.0+ (null safety)
- Must work with Flutter 3.10+
- Cannot modify Dart-ACDC library internals
- Templates must handle all common OpenAPI patterns

## External Dependencies

- **OpenAPI Generator**: https://github.com/OpenAPITools/openapi-generator
- **Dart-ACDC**: https://github.com/jhosm/Dart-ACDC
- **Dio**: https://pub.dev/packages/dio
- **json_serializable**: https://pub.dev/packages/json_serializable

## Development Phases

1. ✅ **Study & Research** - Completed (see `/research/`)
2. 🚧 **Generator Development** - Current phase (proposals created)
3. ⏳ **Refinement** - Future
4. ⏳ **Documentation** - Future
5. ⏳ **Distribution** - Future

## Key Documentation

- **[CLAUDE.md](../CLAUDE.md)** - Developer context and commands
- **[research/project-vision.md](../research/project-vision.md)** - Strategic vision
- **[research/adr-001-generated-code-architecture.md](../research/adr-001-generated-code-architecture.md)** - Architecture decisions
- **[research/dart-generator-quick-reference.md](../research/dart-generator-quick-reference.md)** - Implementation guide
- **[research/dart-acdc-library.md](../research/dart-acdc-library.md)** - Dart-ACDC reference
