# Change: Add Configurable Generator Options

## Why

Different projects have different needs. While the generator should work great with sensible defaults, users need the ability to customize generated code behavior. This proposal adds CLI options and configuration file support for controlling which features are enabled and how code is generated.

## What Changes

- Add CLI options to `DartAcdcClientCodegen` for all configurable features
- Support YAML configuration files for complex option sets
- Add options for enabling/disabling each ACDC feature
- Add options for code style preferences (serialization library, naming)
- Add options for package metadata (name, version, author)
- Add options for default values (cache TTL, log level, etc.)
- Update documentation with all available options

## Impact

- Affected specs: `configuration` (new capability)
- Affected code:
  - `DartAcdcClientCodegen.java` (add CliOption definitions)
  - All templates (conditional blocks based on options)
  - `generator/README.md` (option documentation)

## Dependencies

- Depends on: `add-acdc-integration` (options control ACDC features)

## Blocked By

- `add-acdc-integration` - Need working ACDC integration to make optional

## Blocks

- None (final proposal in Phase 2/3 sequence)
