# Change: Add Full Dart-ACDC Integration

## Why

The basic templates generate working Dart code, but the key value proposition of this generator is **automatic Dart-ACDC integration**. This proposal implements the four pillars of ACDC: Authentication, Caching, Debugging, and Client features.

## What Changes

- Enhance `ApiClient.createDio()` to fully configure all ACDC features
- Generate authentication manager access helpers
- Add offline detection integration
- Configure caching with proper TTL and encryption settings
- Set up logging with sensitive data redaction
- Add certificate pinning support
- Update generated README with ACDC usage examples
- Add authentication workflow examples (login, logout, token management)
- Add error handling examples for each exception type

## Impact

- Affected specs: `acdc-features` (new capability)
- Affected code:
  - `generator/src/main/resources/dart-acdc/api_client.mustache` (major updates)
  - `generator/src/main/resources/dart-acdc/README.mustache` (ACDC documentation)
  - `generator/src/main/resources/dart-acdc/config/*.mustache` (validation)
  - `DartAcdcClientCodegen.java` (ACDC-specific type handling)

## Dependencies

- Depends on: `implement-mustache-templates` (needs working templates)

## Blocked By

- `implement-mustache-templates` - ACDC features build on basic templates

## Blocks

- `add-generator-options` - Options control which ACDC features are enabled
