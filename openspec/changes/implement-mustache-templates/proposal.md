# Change: Implement Mustache Templates for Code Generation

## Why

After bootstrapping the generator structure, we need production-quality Mustache templates that generate Dart code following the patterns established in ADR-001. This is the core of Phase 2 - the templates define exactly what code users will receive.

## What Changes

- Create `model.mustache` for data model classes with `json_serializable`
- Create `remote_data_source.mustache` for abstract API interfaces
- Create `remote_data_source_impl.mustache` for concrete implementations with Dio
- Create `api_client.mustache` for the `ApiClient` factory class
- Create configuration class templates (`acdc_config.mustache`, etc.)
- Create `pubspec.mustache` with all required dependencies
- Create supporting files (`README.mustache`, `analysis_options.mustache`, barrel exports)

## Impact

- Affected specs: `code-generation` (new capability)
- Affected code:
  - `generator/src/main/resources/dart-acdc/model.mustache`
  - `generator/src/main/resources/dart-acdc/remote_data_source.mustache`
  - `generator/src/main/resources/dart-acdc/remote_data_source_impl.mustache`
  - `generator/src/main/resources/dart-acdc/api_client.mustache`
  - `generator/src/main/resources/dart-acdc/config/*.mustache`
  - `generator/src/main/resources/dart-acdc/pubspec.mustache`
  - `generator/src/main/resources/dart-acdc/README.mustache`

## Dependencies

- Depends on: `bootstrap-generator` (needs generator structure in place)

## Blocked By

- `bootstrap-generator` - Cannot create templates without the template directory

## Blocks

- `add-acdc-integration` - ACDC-specific features build on working templates
- `add-generator-options` - Options control template behavior
