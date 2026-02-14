# Contributing to Dart-ACDC Generator

Thank you for your interest in contributing. This guide covers the essentials for getting started.

## Prerequisites

Before contributing, ensure you have the following installed:

- **JDK 21+** and **Maven 3.8+** (for building the generator)
- **Dart SDK 3.0+** and **Flutter 3.10+** (for testing generated code)
- **Git**
- `openapi-generator-cli.jar` v7.10.0 in the project root (see docs/user-guide.md for download instructions)

## Repository Structure

```
Dart-ACDC-generator/
├── generator/       # Java Maven project (the OpenAPI generator itself)
├── mcp-server/      # TypeScript MCP server for AI tool integration
├── scripts/         # Build, generate, and test scripts
├── configs/         # OpenAPI Generator configuration YAMLs
├── samples/
│   ├── specs/       # Test OpenAPI specs
│   └── generated/   # Generated Dart packages
├── docs/            # User guide, testing, contributing, architecture
└── openspec/        # Change proposal specs
```

## Building

Build the generator JAR (includes running tests):

```bash
./scripts/build.sh
```

Build without running tests:

```bash
./scripts/build.sh --skip-tests
```

The built JAR is output to `generator/target/`.

## Testing

### Java Unit Tests

Run the generator's Java unit tests via Maven:

```bash
cd generator && mvn test
```

### Full Pipeline Tests

Generate all sample specs, run build_runner, execute Dart tests, and collect coverage:

```bash
./scripts/test-samples.sh
```

Skip regeneration (test existing generated output):

```bash
./scripts/test-samples.sh --skip-generate
```

Skip both regeneration and build_runner:

```bash
./scripts/test-samples.sh --skip-generate --skip-build
```

### CLI Option Verification

Verify that CLI options produce the expected changes in generated output:

```bash
./scripts/verify-cli-options.sh
```

## Generator Architecture

The generator is built on a 6-layer architecture:

1. **Foundation** -- Configuration constants, name sanitization, type mapping, enum handling, test data generation
2. **Schema Analysis** -- Schema registry, allOf flattening, circular reference detection, schema preprocessing
3. **Model Processing** -- Model creation, oneOf/anyOf union types, discriminator polymorphism
4. **Property & Type Resolution** -- Property creation, request body handling, complex type resolution
5. **Post-Processing** -- Model/operation enrichment, import resolution, final processing passes
6. **Orchestrator** -- `DartAcdcGenerator`, the top-level class that delegates to all layers

All Java source is in `generator/src/main/java/org/openapitools/codegen/languages/`. For detailed architecture documentation, refer to the CLAUDE.md file in the project root.

## Mustache Templates

The Dart code templates are located in:

```
generator/src/main/resources/dart-acdc/
```

There are 21 templates covering API clients, remote data sources, models, configuration, tests, and supporting files. Changes to generated output typically involve editing these templates.

## Workflow

### Proposals

This project uses a proposal-based workflow for non-trivial changes. Before starting significant work (new capabilities, breaking changes, architecture shifts), create a change proposal using the OpenSpec framework:

- Proposals live in the `openspec/` directory
- See `openspec/AGENTS.md` for the spec format and conventions

For small bug fixes and minor improvements, a proposal is not required -- go ahead and submit directly.

### Issue Tracking

This project uses **beads** (bd) for issue tracking:

```bash
bd ready                              # Find work ready to start (no blockers)
bd show <id>                          # View issue details
bd update <id> --status=in_progress   # Claim work
bd close <id>                         # Mark as complete
bd list --status=open                 # View all open issues
bd sync                               # Sync with git remote
```

### Commit Conventions

Use conventional commit messages:

- `feat:` -- A new feature
- `fix:` -- A bug fix
- `docs:` -- Documentation changes
- `test:` -- Adding or updating tests
- `chore:` -- Maintenance tasks (dependencies, CI, etc.)
- `refactor:` -- Code changes that neither fix a bug nor add a feature

Examples:

```
feat: Add support for anyOf union types
fix: Handle circular references in nested allOf schemas
docs: Update contributing guide with testing instructions
test: Add edge case tests for DartEnumHandler
chore: Upgrade OpenAPI Generator to 7.10.0
```

## Development Tips

- **Read the architecture docs** in `docs/architecture/` before making architectural changes.
- **Run the full test pipeline** (`./scripts/test-samples.sh`) before submitting changes to ensure nothing is broken.
- **Template changes** affect all generated output. After modifying a Mustache template, regenerate samples with `./scripts/generate-samples.sh` and verify the output.
- **This is a standalone repo**, not part of the OpenAPI Generator monorepo. All paths start from `generator/`, not `modules/openapi-generator/`.
