<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Dart-ACDC-generator Generator** is a custom OpenAPI Generator that automatically generates Dart API clients fully integrated with the Dart-ACDC library, eliminating manual integration work.

### Problem & Solution

**The Gap**: OpenAPI Generator creates basic API clients, but production Flutter apps need authentication, caching, offline support, and logging—features provided by Dart-ACDC but requiring manual integration.

**The Solution**: A custom OpenAPI Generator that produces production-ready Dart clients with Dart-ACDC fully integrated out of the box.

### Key Technologies

- **Language**: Java (for generator), Dart (for generated code)
- **Base Generator**: Extends/inspired by OpenAPI Generator's dart-dio generator
- **Templating**: Mustache
- **HTTP Client**: Dio (via Dart-ACDC)
- **Target**: Flutter (mobile, desktop, web)

## Architecture & Big Picture

### The Four Pillars of Dart-ACDC (ACDC)

The generator will integrate the four core features of Dart-ACDC:

1. **Authentication (A)**: OAuth 2.1 with automatic token refresh, secure token storage
2. **Caching (C)**: Two-tier caching (Memory + Disk), user isolation, AES-256 encryption
3. **Debugging (D)**: Configurable logging levels, automatic sensitive data redaction
4. **Client (C)**: Request deduplication, certificate pinning, type-safe error handling, offline support

### Development Roadmap (5 Phases)

1. ✅ **Study & Research** - Completed
   - OpenAPI Generator architecture studied
   - Dart-ACDC library fully documented
   - dart-dio generator analyzed
   - Project vision defined

2. **Generator Development** (Next)
   - Bootstrap generator using OpenAPI Generator's `new.sh`
   - Implement Java Codegen class
   - Create Mustache templates for API classes, models, configuration
   - Register generator in OpenAPI Generator's SPI system
   - Test with Petstore OpenAPI spec

3. **Refinement**
   - Add configurable options for ACDC features
   - Improve template quality and edge case handling
   - Add comprehensive unit and integration tests

4. **Documentation**
   - Usage guide and API reference
   - Developer guide for template customization
   - Migration guides from other generators

5. **Distribution**
   - Contribute to OpenAPI Generator repository or maintain fork
   - Package as standalone JAR, Docker image, or NPM package
   - Publish documentation and examples

## Research Documentation

All research findings are located in `/research/`:

- **[project-vision.md](./research/project-vision.md)** - Strategic vision, goals, and features
- **[creating-generators.md](./research/creating-generators.md)** - Deep dive on OpenAPI Generator architecture and custom generator development
- **[dart-generator-quick-reference.md](./research/dart-generator-quick-reference.md)** - Quick-start guide with code examples
- **[dart-acdc-library.md](./research/dart-acdc-library.md)** - Complete Dart-ACDC reference
- **[openapi-generator.md](./research/openapi-generator.md)** - OpenAPI Generator overview

**Start here**: For implementation, read [project-vision.md](./research/project-vision.md) for the full vision, then [dart-generator-quick-reference.md](./research/dart-generator-quick-reference.md) for practical steps.

## Key Technical Concepts

### Generator Architecture (Java)

The generator will consist of:

1. **Codegen Class** - Extends `DefaultCodegen` or `DartDioClientCodegen`
   - Configures type mappings (OpenAPI → Dart types)
   - Defines custom generator options (authentication, caching, logging preferences)
   - Overrides code generation behavior for Dart/ACDC specifics

2. **Mustache Templates** - Generates Dart source files
   - `api.mustache` - API endpoint classes with exception handling
   - `model.mustache` - Data model classes with json_serializable
   - `api_client.mustache` - Main configuration and AcdcClientBuilder setup
   - `pubspec.mustache` - Package configuration with dependencies
   - Supporting files: README, analysis_options.yaml, .gitignore

3. **SPI Registration**
   - Registered in `META-INF/services/org.openapitools.codegen.CodegenConfig`

### Generator Options (Configuration)

The generator will support these configurable properties:

```
Package: pubName, pubVersion, pubDescription, pubAuthor
Features: enableAuthentication, enableCaching, enableOfflineSupport, enableCertificatePinning
Code: useJsonSerializable, useFreezed, nullSafety
Auth: defaultTokenRefreshUrl, useSecureTokenProvider
Cache: defaultCacheTtl, encryptCache
Logging: defaultLogLevel, redactSensitiveData
```

### Generated Code Example

The generator will produce code like this:

```dart
class UserApi {
  final Dio _dio;

  UserApi(this._dio);

  /// Get user by ID
  /// Throws: AcdcAuthException, AcdcNetworkException, AcdcServerException
  Future<User> getUser(String userId) async {
    try {
      final response = await _dio.get('/users/$userId');
      return User.fromJson(response.data);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }
}

class ApiClient {
  static Dio createDio({
    required String baseUrl,
    String? tokenRefreshUrl,
    LogLevel logLevel = LogLevel.info,
  }) {
    return AcdcClientBuilder()
      .withBaseUrl(baseUrl)
      .withAuthentication(tokenRefreshUrl: tokenRefreshUrl ?? '$baseUrl/auth/refresh')
      .withCache(ttl: Duration(hours: 1))
      .withLogging(level: logLevel)
      .withOfflineDetection(failFast: true)
      .build();
  }
}
```

### Type Mappings

The generator defines how OpenAPI types map to Dart:

```
integer/long → int
float/number → double
boolean → bool
string → String
date/DateTime → DateTime
array → List
object → Map<String, dynamic>
```

## Development Prerequisites

- Java Development Kit (JDK) 21+
- Maven 3.8+ (for building the generator)
- Git
- Dart SDK 3.0+ (for testing generated code)
- Flutter 3.10+ (for testing generated code)

## Common Development Commands

### Setting Up

When starting generator development:

```bash
# Clone OpenAPI Generator repository
git clone https://github.com/OpenAPITools/openapi-generator.git
cd openapi-generator

# Bootstrap the dart-acdc-generator generator
./new.sh -n dart-acdc-generator -c -t
```

### Building

```bash
# Build the generator
mvn clean package -DskipTests

# Build with tests
mvn clean package
```

### Testing

```bash
# Test with Petstore spec (standard OpenAPI testing)
./bin/generate-samples.sh ./bin/configs/dart-acdc-generator-petstore.yaml

# Verify generated Dart code
cd samples/client/petstore/dart-acdc-generator
dart analyze
dart test
```

### Key Files to Create/Modify

**Java Codegen Class**:
```
modules/openapi-generator/src/main/java/org/openapitools/codegen/languages/DartAcdcGenerator.java
```

**SPI Registration**:
```
modules/openapi-generator/src/main/resources/META-INF/services/org.openapitools.codegen.CodegenConfig
```

**Mustache Templates**:
```
modules/openapi-generator/src/main/resources/dart-acdc-generator/
├── api.mustache
├── model.mustache
├── api_client.mustache
├── pubspec.mustache
├── README.mustache
├── analysis_options.mustache
└── gitignore.mustache
```

**Test Configuration**:
```
bin/configs/dart-acdc-generator-petstore.yaml
```

## Project Management & Tracking

This project uses **beads** (bd) for issue tracking and multi-session work persistence.

### Essential Commands

```bash
# Find work ready to start (no blockers)
bd ready

# View issue details
bd show <id>

# Create a new issue
bd create --title="..." --type=task|bug|feature --priority=0-4

# Claim work
bd update <id> --status=in_progress

# Mark as complete
bd close <id>

# View all open issues
bd list --status=open

# Sync with git remote
bd sync
```

### Session Protocol

When ending a session, run:

```bash
git status                                    # Check changes
git add <files>                              # Stage code changes
bd sync                                      # Commit beads changes
git commit -m "..."                          # Commit code
bd sync                                      # Sync again
git push                                     # Push to remote
```

## Important Notes

1. **Research First**: All architectural decisions and technical approaches are documented in `/research/`. Read these before implementing.

2. **Template-Driven**: This is a template generation project. Most of the complexity is in designing correct Mustache templates that handle all OpenAPI spec variations.

3. **Incremental Development**: Start with a minimal viable generator (basic API and model templates) and add features incrementally:
   - Phase 1: Basic templates (api.mustache, model.mustache)
   - Phase 2: Configuration and pubspec.mustache
   - Phase 3: Full ACDC integration (authentication, caching, error handling)
   - Phase 4: Advanced features (offline support, certificate pinning)

4. **Testing Strategy**: Use the Petstore OpenAPI spec (standard in OpenAPI Generator) for testing. Verify generated code with `dart analyze` and `dart test`.

5. **Backward Compatibility**: When contributing back to OpenAPI Generator, maintain compatibility with existing dart generators and avoid breaking changes.

## References

- **OpenAPI Generator**: https://openapi-generator.tech/
- **OpenAPI Generator Docs**: https://openapi-generator.tech/docs/
- **Dart-ACDC GitHub**: https://github.com/jhosm/Dart-ACDC
- **Dart-ACDC Docs**: https://github.com/jhosm/Dart-ACDC/tree/main/doc
- **Dio Package**: https://pub.dev/packages/dio
- **Official Generator Guide**: https://openapi-generator.tech/docs/new-generator/
