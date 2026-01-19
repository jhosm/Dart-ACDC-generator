# Change: Bootstrap OpenAPI Generator Structure

## Why

The Dart-ACDC-generator project has completed research (Phase 1) but has no implementation code. Before writing templates or Java classes, we need to establish the foundational generator structure within the OpenAPI Generator ecosystem. This is the first step of Phase 2 (Generator Development) as defined in the project roadmap.

## What Changes

- Set up OpenAPI Generator development environment in this repository
- Bootstrap the `dart-acdc` generator using OpenAPI Generator's scaffolding tools
- Create the basic Java Codegen class structure (`DartAcdcGenerator.java`)
- Register the generator in the SPI system
- Create the template directory structure
- Configure Maven build files
- Add initial test configuration for Petstore spec

## Impact

- Affected specs: `generator-core` (new capability)
- Affected code: New directories and files:
  - `generator/` - Main generator source
  - `generator/src/main/java/org/openapitools/codegen/languages/DartAcdcGenerator.java`
  - `generator/src/main/resources/dart-acdc/` - Template directory
  - `generator/src/main/resources/META-INF/services/org.openapitools.codegen.CodegenConfig`
  - `generator/pom.xml` - Maven build configuration
  - `bin/configs/dart-acdc-petstore.yaml` - Test configuration

## Dependencies

- None (first proposal in sequence)

## Blocked By

- None

## Blocks

- `implement-mustache-templates` - Cannot create templates without the generator structure
- `add-acdc-integration` - ACDC features depend on having working templates
- `add-generator-options` - Options require a working generator
