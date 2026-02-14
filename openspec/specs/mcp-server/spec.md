# mcp-server Specification

## Purpose
TBD - created by archiving change add-mcp-server. Update Purpose after archive.
## Requirements
### Requirement: Generate Tool

The MCP server SHALL expose a `generate` tool that produces a Dart-ACDC API client from an OpenAPI specification.

#### Scenario: Minimal generation with required parameters only
- **WHEN** the `generate` tool is called with `inputSpec` and `outputDir`
- **THEN** the server SHALL generate a Dart package at `outputDir` using default options
- **AND** SHALL return a success message with the output path
- **AND** SHALL include next steps: `dart pub get`, `dart run build_runner build`, `dart analyze`

#### Scenario: Generation with ACDC feature toggles
- **WHEN** the `generate` tool is called with feature parameters (e.g., `enableAuthentication: true`, `enableCaching: false`)
- **THEN** the generated code SHALL reflect the specified feature configuration
- **AND** disabled features SHALL NOT produce config classes in the output

#### Scenario: Generation with package metadata
- **WHEN** the `generate` tool is called with `pubName`, `pubVersion`, and `pubDescription`
- **THEN** the generated `pubspec.yaml` SHALL contain the specified metadata

#### Scenario: Generation with authentication options
- **WHEN** the `generate` tool is called with `defaultTokenRefreshUrl` and `useSecureTokenStorage`
- **THEN** the generated `AuthConfig` SHALL use the specified values as defaults

#### Scenario: Generation with cache options
- **WHEN** the `generate` tool is called with `defaultCacheTtlHours` and `encryptCache`
- **THEN** the generated `CacheConfig` SHALL use the specified values as defaults

#### Scenario: Generation with logging options
- **WHEN** the `generate` tool is called with `defaultLogLevel` and `redactSensitiveData`
- **THEN** the generated `LogConfig` SHALL use the specified values as defaults

#### Scenario: Generation with code style options
- **WHEN** the `generate` tool is called with `serializationLibrary: freezed`
- **THEN** the generated models SHALL use Freezed for serialization

#### Scenario: Relative paths resolved against working directory
- **WHEN** the `generate` tool is called with a relative `inputSpec` or `outputDir` path
- **THEN** the server SHALL resolve the path relative to the current working directory of the MCP server process

#### Scenario: Output directory already exists
- **WHEN** the `generate` tool is called with an `outputDir` that already contains files
- **THEN** the server SHALL overwrite existing generated files
- **AND** SHALL include a warning in the response that the directory was not empty

#### Scenario: Output directory not writable
- **WHEN** the `generate` tool is called with an `outputDir` where the server lacks write permissions
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate the directory is not writable

#### Scenario: Malformed OpenAPI spec
- **WHEN** the `generate` tool is called with a file that is valid YAML/JSON but not a valid OpenAPI specification
- **THEN** the server SHALL return an error with `isError: true`
- **AND** SHALL include the CLI's validation error output verbatim

#### Scenario: Invalid spec path
- **WHEN** the `generate` tool is called with a non-existent `inputSpec` path
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate the file was not found

#### Scenario: Generation timeout
- **WHEN** the Java CLI does not complete within 2 minutes
- **THEN** the server SHALL kill the Java process
- **AND** SHALL return an error with `isError: true` indicating the generation timed out
- **AND** SHALL NOT leave partial output in `outputDir`

#### Scenario: Generator JAR not built triggers auto-build
- **WHEN** the generator JAR does not exist at the expected path
- **THEN** the server SHALL automatically run `./scripts/build.sh --skip-tests` to build the generator
- **AND** SHALL proceed with generation if the build succeeds
- **AND** SHALL return an error with `isError: true` and the build output if the build fails

### Requirement: List Options Tool

The MCP server SHALL expose a `list-options` tool that displays all available generator configuration options.

#### Scenario: List all options
- **WHEN** the `list-options` tool is called with no parameters
- **THEN** the server SHALL return the complete list of dart-acdc generator options
- **AND** each option SHALL include its name, type, description, and default value

#### Scenario: CLI JAR not found
- **WHEN** the openapi-generator-cli.jar is not found
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL include the download URL for the expected version

#### Scenario: Generator JAR not found triggers auto-build
- **WHEN** the generator JAR is not found at the expected path
- **THEN** the server SHALL automatically run `./scripts/build.sh --skip-tests`
- **AND** SHALL retry the `config-help` command if the build succeeds
- **AND** SHALL return an error with `isError: true` and the build output if the build fails

### Requirement: Validate Tool

The MCP server SHALL expose a `validate` tool that checks an OpenAPI specification for compatibility with the dart-acdc generator.

#### Scenario: Valid spec
- **WHEN** the `validate` tool is called with a valid OpenAPI spec
- **THEN** the server SHALL return a success message with `isError` absent or `false`

#### Scenario: Invalid spec
- **WHEN** the `validate` tool is called with an invalid OpenAPI spec
- **THEN** the server SHALL return the CLI's validation error output verbatim with `isError: true`

#### Scenario: Spec with warnings
- **WHEN** the `validate` tool is called with a spec that has non-fatal issues
- **THEN** the server SHALL return the warnings in the content text with `isError` absent or `false`

#### Scenario: Non-existent spec file
- **WHEN** the `validate` tool is called with a path that does not exist
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate the file was not found

#### Scenario: Non-OpenAPI file
- **WHEN** the `validate` tool is called with a file that is not YAML or JSON (e.g., a binary file)
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate the file format is not supported

### Requirement: Server Configuration

The MCP server SHALL be configurable via environment variables for locating required JARs.

#### Scenario: Default JAR discovery
- **WHEN** `DART_ACDC_PROJECT_ROOT` is set
- **THEN** the server SHALL look for `openapi-generator-cli.jar` in the project root
- **AND** SHALL look for `dart-acdc-generator-*.jar` in `generator/target/`

#### Scenario: Multiple generator JARs found
- **WHEN** `generator/target/` contains multiple JARs matching `dart-acdc-generator-*.jar`
- **THEN** the server SHALL select the most recently modified JAR

#### Scenario: Explicit JAR paths
- **WHEN** `OPENAPI_CLI_JAR` and `DART_ACDC_GENERATOR_JAR` environment variables are set
- **THEN** the server SHALL use those paths instead of auto-discovery

#### Scenario: Auto-build on missing generator JAR
- **WHEN** the generator JAR is not found at the expected path
- **AND** `DART_ACDC_PROJECT_ROOT` is set or can be inferred
- **THEN** the server SHALL automatically execute `./scripts/build.sh --skip-tests` to build the JAR
- **AND** SHALL retry JAR discovery after build completes

#### Scenario: Auto-build fails
- **WHEN** the auto-build is triggered but `./scripts/build.sh --skip-tests` exits with a non-zero code
- **THEN** the server SHALL return an error with `isError: true`
- **AND** SHALL include the build script's stdout and stderr in the error message

#### Scenario: Auto-build succeeds but JAR still not found
- **WHEN** the auto-build completes successfully but the generator JAR is still not found in `generator/target/`
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate the build succeeded but the JAR was not produced

#### Scenario: Maven not installed
- **WHEN** the auto-build is triggered but Maven is not installed or not on PATH
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate that Maven 3.8+ is required to build the generator

#### Scenario: No configuration provided
- **WHEN** neither `DART_ACDC_PROJECT_ROOT`, `OPENAPI_CLI_JAR`, nor `DART_ACDC_GENERATOR_JAR` are set
- **AND** the project root cannot be inferred from the server's location
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL list the required environment variables

#### Scenario: Missing Java runtime
- **WHEN** Java is not installed or not on PATH
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate that Java 21+ is required and suggest `java --version` to verify

### Requirement: stdio Transport

The MCP server SHALL communicate via stdio transport for compatibility with local AI coding tools.

#### Scenario: Claude Code integration
- **WHEN** the server is configured in Claude Code's `mcpServers` settings with `command: "node"` and the server's entry point
- **THEN** the server SHALL start and respond to MCP tool calls over stdin/stdout

#### Scenario: npx invocation
- **WHEN** the server is configured with `command: "npx"` and the npm package name `@dart-acdc/mcp-server`
- **THEN** the server SHALL be automatically downloaded and started

### Requirement: Typed Input Schemas

All MCP tools SHALL expose Zod-based input schemas that match the generator's CLI options.

#### Scenario: Parameter discoverability
- **WHEN** an MCP client requests the tool list
- **THEN** each tool SHALL include a JSON Schema description of its parameters
- **AND** each parameter SHALL have a type, description, and default value where applicable

#### Scenario: Input validation
- **WHEN** a tool is called with invalid parameter types (e.g., string where number expected)
- **THEN** the server SHALL return an error with `isError: true` containing the Zod validation error message
- **AND** SHALL NOT invoke the Java CLI

### Requirement: Platform Support

The MCP server SHALL target Unix-like operating systems (macOS, Linux).

#### Scenario: Unix classpath separator
- **WHEN** the server constructs the Java classpath
- **THEN** it SHALL use `:` as the classpath separator

#### Scenario: Windows not supported
- **WHEN** the server detects a Windows operating system
- **THEN** the server SHALL return an error with `isError: true`
- **AND** the error message SHALL indicate that Windows is not currently supported

