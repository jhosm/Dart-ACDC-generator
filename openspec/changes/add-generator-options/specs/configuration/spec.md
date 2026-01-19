# Capability: Configuration

Configurable generator options that control code generation behavior and feature enablement.

## ADDED Requirements

### Requirement: Package Metadata Configuration

The generator SHALL accept package metadata options that customize the generated pubspec.yaml.

#### Scenario: pubName applied to output
- **WHEN** `pubName` is set to `my_api_client`
- **THEN** the generated pubspec.yaml SHALL have `name: my_api_client`
- **AND** the main barrel export SHALL be `lib/my_api_client.dart`

#### Scenario: Default pubName
- **WHEN** `pubName` is not provided
- **THEN** the generator SHALL derive it from OpenAPI `info.title` (sanitized to valid Dart package name)
- **AND** SHALL use `openapi_client` if title is missing or sanitization results in empty string

#### Scenario: Optional metadata options
- **WHEN** `pubVersion`, `pubDescription`, `pubAuthor`, and `pubHomepage` are provided
- **THEN** these values SHALL appear in the generated pubspec.yaml

#### Scenario: Default pubVersion
- **WHEN** `pubVersion` is not provided
- **THEN** the generator SHALL derive it from OpenAPI `info.version`, or use `1.0.0` if version is missing

#### Scenario: Default pubDescription
- **WHEN** `pubDescription` is not provided
- **THEN** the generator SHALL derive it from OpenAPI `info.description`, or use empty string if description is missing

### Requirement: Feature Toggle Options

The generator SHALL support enabling/disabling each ACDC feature independently.

#### Scenario: Authentication disabled
- **WHEN** `enableAuthentication` is set to `false`
- **THEN** the generated `ApiClient.createDio()` SHALL NOT include authentication setup
- **AND** `AuthConfig` class SHALL NOT be generated

#### Scenario: Caching disabled
- **WHEN** `enableCaching` is set to `false`
- **THEN** the generated `ApiClient.createDio()` SHALL NOT include cache setup
- **AND** `CacheConfig` class SHALL NOT be generated

#### Scenario: Logging disabled
- **WHEN** `enableLogging` is set to `false`
- **THEN** the generated `ApiClient.createDio()` SHALL NOT include logging setup
- **AND** `LogConfig` class SHALL NOT be generated

#### Scenario: Offline support disabled
- **WHEN** `enableOfflineSupport` is set to `false`
- **THEN** the generated `ApiClient.createDio()` SHALL NOT include offline detection setup
- **AND** `OfflineConfig` class SHALL NOT be generated

#### Scenario: Certificate pinning disabled by default
- **WHEN** `enableCertificatePinning` is not specified
- **THEN** certificate pinning SHALL NOT be configured (default: false)

#### Scenario: All features enabled by default
- **WHEN** no feature toggle options are specified
- **THEN** authentication, caching, logging, and offline support SHALL be enabled by default

### Requirement: Default Value Configuration

The generator SHALL accept options that set default values for runtime configuration.

#### Scenario: Custom default log level
- **WHEN** `defaultLogLevel` is set to `debug`
- **THEN** the generated `LogConfig` SHALL use `LogLevel.debug` as the default

#### Scenario: Custom default cache TTL
- **WHEN** `defaultCacheTtlHours` is set to `2`
- **THEN** the generated `CacheConfig` SHALL use `Duration(hours: 2)` as the default TTL

#### Scenario: Custom token refresh threshold
- **WHEN** `refreshThresholdMinutes` is set to `10`
- **THEN** the generated `AuthConfig` SHALL use `Duration(minutes: 10)` as the default threshold

### Requirement: Code Generation Options

The generator SHALL accept options that control code style and structure.

#### Scenario: Interface generation disabled
- **WHEN** `generateInterfaces` is set to `false`
- **THEN** only implementation classes SHALL be generated (no abstract interfaces)

#### Scenario: Custom data source suffix
- **WHEN** `dataSourceSuffix` is set to `Api`
- **THEN** API classes SHALL be named like `UserApi` instead of `UserRemoteDataSource`

#### Scenario: Barrel exports disabled
- **WHEN** `generateBarrelExports` is set to `false`
- **THEN** barrel export files SHALL NOT be generated

### Requirement: Option Documentation

The generator SHALL provide comprehensive documentation for all options.

#### Scenario: Help command lists all options
- **WHEN** the generator help is displayed
- **THEN** all configurable options SHALL be listed with descriptions and default values

#### Scenario: Option validation errors are clear
- **WHEN** an invalid option value is provided (e.g., invalid log level)
- **THEN** the error message SHALL clearly indicate the valid values

### Requirement: Configuration File Support

The generator SHALL support YAML configuration files for specifying options.

#### Scenario: YAML config file accepted
- **WHEN** a config file is provided with generator options
- **THEN** the options SHALL be applied to code generation

#### Scenario: CLI options override config file
- **WHEN** both a config file and CLI options are provided
- **THEN** CLI options SHALL take precedence over config file values

#### Scenario: Example config files provided
- **WHEN** the generator is distributed
- **THEN** example configuration files SHALL be provided for common use cases
