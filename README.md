# Dart-ACDC Generator

> **Generate production-ready Dart API clients with built-in authentication, caching, debugging, and offline support**

A custom OpenAPI Generator that automatically generates Dart API clients fully integrated with the [Dart-ACDC](https://github.com/jhosm/Dart-ACDC) library, eliminating manual integration work and providing enterprise-grade features out of the box.

## What is This?

**The Problem**: OpenAPI Generator creates basic Dart API clients, but production Flutter apps need authentication, caching, offline support, and logging—features provided by Dart-ACDC but requiring manual integration.

**The Solution**: This generator produces Dart clients with Dart-ACDC **fully integrated from the start**, giving you:

- 🔐 **Authentication** - OAuth 2.1 with automatic token refresh
- 💾 **Caching** - Two-tier caching (memory + disk) with AES-256 encryption
- 🐛 **Debugging** - Configurable logging with automatic sensitive data redaction
- 🌐 **Offline Support** - Request deduplication, certificate pinning, type-safe error handling

## Quick Start

### Prerequisites

- **JDK 21+** (for building the generator)
- **Maven 3.8+** (for building the generator)
- **Dart SDK 3.0+** / **Flutter 3.10+** (for running generated code)

### 1. Build the Generator

```bash
cd generator
mvn clean package
```

### 2. Generate Your Client

```bash
java -cp "generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar:openapi-generator-cli.jar" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g dart-acdc \
  -i path/to/your/openapi.yaml \
  -o output/directory \
  --additional-properties=pubName=my_api_client
```

### 3. Use the Generated Client

```dart
import 'package:my_api_client/my_api_client.dart';

// Create configured API client
final dio = ApiClient.createDio(
  AcdcConfig(
    baseUrl: 'https://api.example.com',
    auth: AuthConfig(
      tokenRefreshUrl: 'https://api.example.com/auth/refresh',
    ),
    log: LogConfig(level: LogLevel.info),
  ),
);

// Use the API (via RemoteDataSource pattern)
final userApi = UserRemoteDataSourceImpl(dio);
final user = await userApi.getUser('user-123');
```

## Features

### Advanced OpenAPI Support

- ✅ **allOf, oneOf, anyOf** - Full schema composition support
- ✅ **Discriminated Unions** - Generates Dart 3 sealed classes
- ✅ **Nested Composition** - Handles complex schema hierarchies
- ✅ **Circular References** - Automatic detection and nullable type handling
- ✅ **Enums** - Collision-resistant naming with original value preservation
- ✅ **Reserved Keywords** - Automatic escaping for Dart reserved words

### Code Quality

- 📐 **Flutter Architecture** - Follows clean architecture with `remote_data_sources/` pattern
- 🎯 **Type Safety** - Strong Dart typing with null safety support
- 📦 **Package Conventions** - Proper naming, structure, and pubspec configuration
- 🔧 **Customizable** - Configurable package name, version, and description

## Documentation

### User Documentation

- **[How-To Guide](docs/HOW-TO.md)** - Complete step-by-step guide for using the generator
- **[Generator Documentation](generator/README.md)** - Detailed build, usage, and troubleshooting guide

### Developer Documentation

- **[Development Guide](CLAUDE.md)** - Guide for AI assistants and developers
- **[Project Vision](research/project-vision.md)** - Strategic vision, goals, and features
- **[Architecture Decisions](research/adr-001-generated-code-architecture.md)** - ADR for generated code structure
- **[OpenSpec Proposals](openspec/)** - Formal specifications and change proposals

## Project Status

**Current Phase**: Phase 2 - Generator Development (95% Complete)

### What's Implemented ✅

- [x] Standalone generator architecture
- [x] Dart type mappings (primitives, collections, dates, files)
- [x] Schema composition (allOf, oneOf, anyOf)
- [x] Nested and circular schema handling
- [x] Reserved keyword escaping
- [x] Enum generation with collision handling
- [x] File upload support (MultipartFile)
- [x] Comprehensive test suite (39 tests passing)
- [x] Documentation

### Current Limitations ⚠️

The generator currently produces **complete code** for:
- oneOf/anyOf sealed class hierarchies
- Enum types
- API client configuration

**Note**: Regular object model generation shows TODO placeholders. Full model template implementation is planned for Phase 3.

### What's Next

**Phase 3**: Refinement
- Implement full model templates (regular objects)
- Add configurable options for ACDC features
- Improve edge case handling
- Add integration tests

**Phase 4**: Documentation
- Usage guide and API reference
- Developer guide for template customization
- Migration guides from other generators

**Phase 5**: Distribution
- Package as standalone JAR
- Docker image
- NPM package
- Contribute to OpenAPI Generator repository

## Architecture

### Generated Code Structure

```
output/
├── lib/
│   ├── my_api.dart           # Barrel export
│   ├── api_client.dart       # ApiClient factory with ACDC config
│   ├── config/               # Configuration classes
│   ├── models/               # Data models (from schemas)
│   └── remote_data_sources/  # API classes (from paths)
├── pubspec.yaml
└── README.md
```

### Generator Architecture

```
generator/
├── src/main/java/            # Generator implementation
│   └── DartAcdcGenerator.java
├── src/main/resources/       # Mustache templates
│   └── dart-acdc/
│       ├── model.mustache
│       ├── remote_data_source.mustache
│       ├── remote_data_source_impl.mustache
│       └── ...
└── src/test/java/            # Unit tests
```

## Examples

See the `samples/` directory for example OpenAPI specs and generated output:

- **`minimal.yaml`** - Minimal API example for quick testing
- **`petstore.yaml`** - Classic Petstore API example
- **`composition.yaml`** - Schema composition examples (allOf, oneOf, anyOf)
- **`enums.yaml`** - Enum generation examples
- **`file-upload.yaml`** - File upload examples
- **`reserved-words.yaml`** - Reserved keyword handling

Generate any example:

```bash
java -cp "generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar:openapi-generator-cli.jar" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g dart-acdc \
  -i samples/petstore.yaml \
  -o samples/generated/petstore
```

## Development

### Repository Structure

```
.
├── generator/              # Generator implementation (Java + Mustache)
├── samples/               # Example OpenAPI specs
├── research/              # Design documents and research
├── openspec/              # Formal specifications and proposals
├── scripts/               # Build and development scripts
└── docs/                  # Additional documentation
```

### Building from Source

```bash
# Build the generator
cd generator
mvn clean package

# Run tests
mvn test

# Run specific test
mvn test -Dtest=DartAcdcGeneratorTest
```

### Using Beads for Issue Tracking

This project uses [beads](https://github.com/beadsinc/beads) for git-backed issue tracking:

```bash
# Find work
bd ready

# Show issue details
bd show <issue-id>

# Update issue status
bd update <issue-id> --status=in_progress

# Close completed work
bd close <issue-id>
```

## Version Compatibility

| Component | Version | Notes |
|-----------|---------|-------|
| OpenAPI Generator | 7.10.0 | Pinned for reproducible builds |
| JDK | 21+ | Build requirement |
| Maven | 3.8+ | Build tool |
| Dart SDK | 3.0+ | For generated code |
| Flutter | 3.10+ | For Flutter apps |
| Dart-ACDC | 1.0+ | Runtime dependency (generated code) |

## Contributing

Contributions are welcome! This project follows a proposal-based workflow using OpenSpec:

1. **Propose Changes**: Create a proposal in `openspec/changes/`
2. **Get Approval**: Submit for review
3. **Implement**: Follow the approved specification
4. **Test**: Ensure all tests pass
5. **Document**: Update relevant documentation

See [CLAUDE.md](CLAUDE.md) for detailed development guidelines.

## Resources

### Related Projects

- **[Dart-ACDC Library](https://github.com/jhosm/Dart-ACDC)** - The ACDC library this generator integrates with
- **[OpenAPI Generator](https://openapi-generator.tech/)** - The base generator framework

### Documentation

- **[OpenAPI Specification](https://swagger.io/specification/)** - OpenAPI 3.0 spec
- **[Dart Language](https://dart.dev/)** - Dart programming language
- **[Flutter](https://flutter.dev/)** - Flutter framework
- **[Mustache Templates](https://mustache.github.io/)** - Templating engine used

### Learning Resources

- **[Creating Custom Generators](research/creating-generators.md)** - Guide to OpenAPI Generator architecture
- **[Dart Generator Quick Reference](research/dart-generator-quick-reference.md)** - Quick start guide
- **[Dart-ACDC Library Docs](research/dart-acdc-library.md)** - Complete ACDC reference

## License

[Add license information here]

## Acknowledgments

- **OpenAPI Generator** community for the extensible framework
- **Dart-ACDC** developers for the comprehensive Flutter HTTP library
- Contributors and early adopters providing feedback and testing

---

**Need Help?** Check the [generator documentation](generator/README.md) or open an issue.
