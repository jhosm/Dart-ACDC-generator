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

**Dart-ACDC Generator** is a custom OpenAPI Generator that produces Dart API clients fully integrated with the Dart-ACDC library, eliminating manual integration work.

**The Gap**: OpenAPI Generator creates basic API clients, but production Flutter apps need authentication, caching, offline support, and logging — features provided by Dart-ACDC but requiring manual integration.

### Key Technologies

- **Language**: Java 21 (generator), Dart (generated code)
- **Build**: Maven, OpenAPI Generator 7.10.0
- **Templating**: Mustache
- **HTTP Client**: Dio (via Dart-ACDC)
- **Target**: Flutter (mobile, desktop, web)

### The Four Pillars of Dart-ACDC (ACDC)

1. **Authentication (A)**: OAuth 2.1, automatic token refresh, secure token storage
2. **Caching (C)**: Two-tier (Memory + Disk), user isolation, AES-256 encryption
3. **Debugging (D)**: Configurable log levels, automatic sensitive data redaction
4. **Client (C)**: Request deduplication, certificate pinning, type-safe errors, offline support

## Directory Structure

```text
Dart-ACDC-generator/
├── generator/                          # Java Maven project
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/.../languages/     # 23 Java classes (see Architecture)
│       │   └── resources/
│       │       ├── dart-acdc/          # 21 Mustache templates
│       │       └── META-INF/services/  # SPI registration
│       └── test/java/.../languages/    # 19 Java test classes
├── mcp-server/                         # MCP server for AI tool integration (TypeScript)
│   ├── src/
│   │   ├── index.ts                    # Server entry point (stdio transport)
│   │   ├── tools/                      # generate, list-options, validate tools
│   │   └── utils/runner.ts             # CLI runner (JAR discovery, auto-build)
│   └── test/                           # Vitest tests
├── scripts/                            # Build, generate, and test scripts
├── configs/                            # OpenAPI Generator config YAMLs
├── samples/
│   ├── specs/                          # Test OpenAPI specs (petstore, minimal, etc.)
│   └── generated/                      # Generated Dart packages
├── docs/
│   ├── user-guide.md                   # Step-by-step usage guide
│   ├── testing.md                      # Testing strategy and commands
│   ├── contributing.md                 # Developer workflow and contribution guide
│   └── architecture/                   # Architecture research & decisions
├── openspec/                           # Change proposal specs
└── openapi-generator-cli.jar           # OpenAPI Generator CLI (not checked in)
```

## Common Development Commands

### Building

```bash
# Build generator JAR (with tests)
./scripts/build.sh

# Build without tests
./scripts/build.sh --skip-tests
```

### Generating Samples

```bash
# Generate all sample specs (petstore, minimal, composition, file-upload, enums, reserved-words)
./scripts/generate-samples.sh

# Generate a single spec
./scripts/generate-samples.sh petstore
```

### Testing

```bash
# Java unit tests only (via Maven)
cd generator && mvn test

# Full pipeline: generate samples + build_runner + Dart tests + coverage
./scripts/test-samples.sh

# Dart tests only (skip regeneration)
./scripts/test-samples.sh --skip-generate

# Skip build_runner too (use existing .g.dart files)
./scripts/test-samples.sh --skip-generate --skip-build

# Verify CLI options affect generated output
./scripts/verify-cli-options.sh
```

### Prerequisites

- JDK 21+, Maven 3.8+, Git
- Dart SDK 3.0+, Flutter 3.10+ (for testing generated code)
- `openapi-generator-cli.jar` in project root (v7.10.0)

## Generator Architecture (Java)

All Java source lives in `generator/src/main/java/org/openapitools/codegen/languages/`.

The generator uses a **6-layer architecture** where each layer depends only on layers below it:

### Layer 1 — Foundation (no dependencies on other layers)

| Class                              | Responsibility                                                                   |
| ---------------------------------- | -------------------------------------------------------------------------------- |
| **DartGeneratorConfig**            | Centralized configuration constants (reserved words, type mappings, CLI options) |
| **DartNameSanitizer**              | Package name sanitization, reserved word escaping, case conversion               |
| **DartTypeMapper**                 | OpenAPI to Dart type mapping, context-aware (multipart vs non-multipart)         |
| **DartEnumHandler**                | Enum variable naming, collision resolution, numeric value prefixing              |
| **DartTestDataGenerator**          | Test value generation for primitives, complex types, and MultipartFile           |

### Layer 2 — Schema Analysis

| Class                              | Responsibility                                                                |
| ---------------------------------- | ----------------------------------------------------------------------------- |
| **DartSchemaRegistry**             | Tracks all schemas seen during generation for cross-reference                 |
| **DartAllOfFlattener**             | Flattens allOf compositions into single models                                |
| **DartCircularReferenceDetector**  | Detects and breaks circular `$ref` chains                                     |
| **DartSchemaPreprocessor**         | Preprocesses OpenAPI schemas before code generation                           |

### Layer 3 — Model Processing

| Class                              | Responsibility                                                                |
| ---------------------------------- | ----------------------------------------------------------------------------- |
| **DartModelFactory**               | Creates CodegenModel objects from OpenAPI schemas                              |
| **DartOneOfProcessor**             | Handles oneOf union types                                                     |
| **DartAnyOfProcessor**             | Handles anyOf union types                                                     |
| **DartDiscriminatorProcessor**     | Handles discriminator-based polymorphism                                      |

### Layer 4 — Property & Type Resolution

| Class                              | Responsibility                                                                |
| ---------------------------------- | ----------------------------------------------------------------------------- |
| **DartPropertyFactory**            | Creates CodegenProperty objects                                               |
| **DartRequestBodyFactory**         | Creates request body parameters                                               |
| **DartTypeResolver**               | Resolves complex/nested type references                                       |

### Layer 5 — Post-Processing

| Class                              | Responsibility                                                                |
| ---------------------------------- | ----------------------------------------------------------------------------- |
| **DartModelEnricher**              | Enriches models with additional metadata after creation                       |
| **DartModelImportResolver**        | Resolves import statements for model files                                    |
| **DartModelPostProcessor**         | Final model post-processing pass                                              |
| **DartOperationEnricher**          | Enriches operations with return types, parameter metadata                     |
| **DartOperationImportResolver**    | Resolves import statements for operation files                                |
| **DartOperationPostProcessor**     | Final operation post-processing pass                                          |

### Layer 6 — Orchestrator

| Class                              | Responsibility                                                                |
| ---------------------------------- | ----------------------------------------------------------------------------- |
| **DartAcdcGenerator**              | Top-level orchestrator. Extends `DefaultCodegen`, delegates to all layers.    |

### Mustache Templates

Located in `generator/src/main/resources/dart-acdc/`:

**Core templates:**

- `api_client.mustache` — Main AcdcClientBuilder configuration
- `remote_data_source.mustache` — API interface (abstract class per tag)
- `remote_data_source_impl.mustache` — API implementation with exception handling
- `model.mustache` — Single model class with json_serializable
- `models.mustache` — Barrel file exporting all models
- `remote_data_sources.mustache` — Barrel file exporting all API classes
- `library.mustache` — Top-level library barrel file
- `pubspec.mustache` — Package dependencies

**Config templates:**

- `config.mustache`, `acdc_config.mustache`, `auth_config.mustache`, `cache_config.mustache`, `log_config.mustache`, `offline_config.mustache`, `security_config.mustache`

**Test templates:**

- `test/api_test.mustache`, `test/model_test.mustache`, `test/test_helpers.mustache`

**Supporting:**

- `README.mustache`, `analysis_options.mustache`, `gitignore.mustache`

### SPI Registration

`generator/src/main/resources/META-INF/services/org.openapitools.codegen.CodegenConfig`

### Generator Options

```text
Package: pubName, pubVersion, pubDescription, pubAuthor
Features: enableAuthentication, enableCaching, enableOfflineSupport, enableCertificatePinning
Code: useJsonSerializable, useFreezed, nullSafety
Auth: defaultTokenRefreshUrl, useSecureTokenProvider
Cache: defaultCacheTtl, encryptCache
Logging: defaultLogLevel, redactSensitiveData
```

### Type Mappings (OpenAPI → Dart)

```text
integer/long → int       float/number → double     boolean → bool
string → String          date/DateTime → DateTime   array → List
object → Map<String, dynamic>
```

## Development Roadmap

1. ✅ **Study & Research** — Completed
2. ✅ **Generator Development** — Completed (23 Java classes, 21 templates, 19 test classes)
3. 🔄 **Refinement & ACDC Integration** — In Progress (adding ACDC features, tests, edge cases)
4. ✅ **MCP Server** — Completed (TypeScript MCP server with generate, list-options, validate tools)
5. **Documentation** — Next
6. **Distribution** — Future

## Architecture Documentation

All architecture docs in `/docs/architecture/`:

- **[project-vision.md](./docs/architecture/project-vision.md)** — Strategic vision and goals
- **[creating-generators.md](./docs/architecture/creating-generators.md)** — OpenAPI Generator architecture deep dive
- **[dart-generator-quick-reference.md](./docs/architecture/dart-generator-quick-reference.md)** — Quick-start guide with code examples
- **[dart-acdc-library.md](./docs/architecture/dart-acdc-library.md)** — Dart-ACDC library reference
- **[openapi-generator.md](./docs/architecture/openapi-generator.md)** — OpenAPI Generator overview
- **[adr-001-generated-code-architecture.md](./docs/architecture/adr-001-generated-code-architecture.md)** — Architecture Decision Record

## Important Notes

1. **Architecture First**: Read `/docs/architecture/` docs before implementing architectural changes.
2. **Template-Driven**: Most complexity is in Mustache templates handling all OpenAPI spec variations.
3. **Standalone Repo**: This is NOT inside the OpenAPI Generator monorepo. All paths start from `generator/`, not `modules/openapi-generator/`.
4. **Template directory is `dart-acdc`**, not `dart-acdc-generator`.

## Project Management & Tracking

This project uses **beads** (bd) for issue tracking and multi-session work persistence.

### Essential Commands

```bash
bd ready                              # Find work ready to start (no blockers)
bd show <id>                          # View issue details
bd create --title="..." --type=task|bug|feature --priority=0-4
bd update <id> --status=in_progress   # Claim work
bd close <id>                         # Mark as complete
bd list --status=open                 # View all open issues
bd sync                               # Sync with git remote
```

### Session Protocol

When ending a session, run:

```bash
git status              # Check changes
git add <files>         # Stage code changes
bd sync                 # Commit beads changes
git commit -m "..."     # Commit code
bd sync                 # Sync again
git push                # Push to remote
```

## References

- [OpenAPI Generator](https://openapi-generator.tech/) | [Docs](https://openapi-generator.tech/docs/) | [New Generator Guide](https://openapi-generator.tech/docs/new-generator/)
- [Dart-ACDC GitHub](https://github.com/jhosm/Dart-ACDC) | [Dart-ACDC Docs](https://github.com/jhosm/Dart-ACDC/tree/main/doc)
- [Dio Package](https://pub.dev/packages/dio)
