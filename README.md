# Dart-ACDC Generator

> Generate production-ready Dart API clients with built-in authentication, caching, debugging, and offline support.

A custom [OpenAPI Generator](https://openapi-generator.tech/) that produces Dart API clients fully integrated with the [Dart-ACDC](https://github.com/jhosm/Dart-ACDC) library, eliminating manual integration work and providing enterprise-grade features out of the box.

## Quick Start

```bash
# 1. Build the generator
cd generator && mvn clean package && cd ..

# 2. Generate your client
java -cp "generator/target/dart-acdc-generator-1.0.0-SNAPSHOT.jar:openapi-generator-cli.jar" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g dart-acdc \
  -i path/to/your/openapi.yaml \
  -o output/directory \
  --additional-properties=pubName=my_api_client

# 3. Build and use
cd output/directory && dart pub get && dart run build_runner build
```

```dart
import 'package:my_api_client/my_api_client.dart';

final dio = await ApiClient.createDio(
  AcdcConfig(
    baseUrl: 'https://api.example.com',
    auth: AuthConfig(tokenRefreshUrl: 'https://api.example.com/auth/refresh'),
    log: LogConfig(level: LogLevel.info),
  ),
);

final userApi = UserRemoteDataSourceImpl(dio);
final user = await userApi.getUser('user-123');
```

## Features

**ACDC Integration** -- OAuth 2.1 with automatic token refresh, two-tier caching (memory + disk) with AES-256 encryption, configurable logging with sensitive data redaction, offline detection, and certificate pinning.

**OpenAPI Support** -- Full schema composition (allOf, oneOf, anyOf), discriminated unions via Dart 3 sealed classes, circular reference detection, enum collision handling, and reserved keyword escaping.

**Code Quality** -- Clean architecture with `RemoteDataSource` pattern, strong Dart typing with null safety, interface/implementation split for testability, and generated tests.

## Repository Structure

```
Dart-ACDC-generator/
├── generator/         # Java generator (Maven project, 23 classes, 21 Mustache templates)
├── mcp-server/        # MCP server for AI tool integration (TypeScript)
├── scripts/           # Build, generate, and test automation
├── configs/           # OpenAPI Generator config YAMLs
├── samples/
│   ├── specs/         # Test OpenAPI specs (petstore, composition, enums, etc.)
│   └── generated/     # Generated Dart packages
├── docs/              # User guide, testing, contributing, architecture
└── openspec/          # Change proposal specifications
```

## Prerequisites

| Component | Version | Purpose |
|-----------|---------|---------|
| JDK | 21+ | Build the generator |
| Maven | 3.8+ | Build tool |
| Dart SDK | 3.0+ | Run generated code |
| Flutter | 3.10+ | Flutter apps |
| OpenAPI Generator | 7.10.0 | `openapi-generator-cli.jar` in project root |

## Documentation

- **[User Guide](docs/user-guide.md)** -- Step-by-step guide for generating and using API clients
- **[Generator README](generator/README.md)** -- Build, usage, schema composition, and troubleshooting
- **[MCP Server](mcp-server/README.md)** -- AI tool integration via Model Context Protocol
- **[Testing](docs/testing.md)** -- Testing strategy and commands
- **[Contributing](docs/contributing.md)** -- Developer workflow and building from source
- **[Architecture](docs/architecture/)** -- Design decisions, vision, and research

## Project Status

| Phase | Status |
|-------|--------|
| Study & Research | Completed |
| Generator Development (23 classes, 21 templates) | Completed |
| Refinement & ACDC Integration | In Progress |
| MCP Server (generate, validate, list-options) | Completed |
| Documentation | In Progress |
| Distribution | Future |

## Examples

See `samples/specs/` for test OpenAPI specs and `samples/generated/` for generated output:

| Spec | Tests |
|------|-------|
| `petstore.yaml` | Standard REST API |
| `composition.yaml` | allOf, oneOf, anyOf schemas |
| `enums.yaml` | Enum collision handling |
| `file-upload.yaml` | Multipart file uploads |
| `reserved-words.yaml` | Dart reserved keyword escaping |
| `minimal.yaml` | Minimal edge case testing |

Generate any example:

```bash
./scripts/generate-samples.sh petstore
```

## License

[Apache License 2.0](LICENSE)

## Resources

- [Dart-ACDC Library](https://github.com/jhosm/Dart-ACDC) | [Docs](https://github.com/jhosm/Dart-ACDC/tree/main/doc)
- [OpenAPI Generator](https://openapi-generator.tech/) | [New Generator Guide](https://openapi-generator.tech/docs/new-generator/)
- [Dio Package](https://pub.dev/packages/dio)
