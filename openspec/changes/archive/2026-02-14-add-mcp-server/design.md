## Context

The Dart-ACDC generator is a Java CLI tool. End users invoke it via:

```
java -cp cli.jar:generator.jar org.openapitools.codegen.OpenAPIGenerator generate -g dart-acdc -c config.yaml
```

AI coding assistants need a programmatic, discoverable interface to invoke this. MCP (Model Context Protocol) is the emerging standard for tool-to-AI integration, supported by Claude Code, Cursor, Windsurf, and others.

### Stakeholders

- **End users**: Flutter developers who want AI-assisted API client generation
- **Generator maintainers**: Need minimal maintenance burden from the MCP layer
- **AI tool vendors**: Consume MCP servers via stdio or HTTP transport

## Goals / Non-Goals

### Goals

- Expose the generator as typed, discoverable MCP tools
- Zero changes to existing Java generator code
- Simple installation: `npx @dart-acdc/mcp-server` or local path
- stdio transport for Claude Code compatibility
- Typed input schemas matching the generator's CLI options

### Non-Goals

- HTTP/SSE transport (future, not in this proposal)
- MCP Resources or Prompts (tools only for now)
- Auto-generating Zod schemas from Java CliOption definitions (future optimization)
- Modifying the Java generator to expose a programmatic API
- GUI or web interface
- Windows support (Unix-like only: macOS, Linux)

## Decisions

### Decision 1: TypeScript + stdio

**Choice**: TypeScript with `@modelcontextprotocol/server` SDK, stdio transport.

**Rationale**:
- The MCP TypeScript SDK is the most mature and best documented
- stdio is the standard transport for locally-spawned MCP servers in Claude Code
- TypeScript provides strong typing for tool schemas (via Zod)
- The server is a thin wrapper (~200 lines) — no complex logic to maintain

**Alternatives considered**:
- **Java MCP SDK**: Would avoid Node.js dependency, but the Java MCP SDK is less mature and would couple the server to the generator's build. Wrapping a CLI is simpler than programmatic Java integration.
- **Python MCP SDK**: Viable, but TypeScript has better SDK ecosystem and tooling for MCP.

### Decision 2: Thin CLI Wrapper (not programmatic integration)

**Choice**: Shell out to `java -cp ... OpenAPIGenerator generate ...` via `child_process`.

**Rationale**:
- Decouples the MCP server from the generator's internal Java APIs
- Generator JAR version can be swapped independently
- Matches how users already invoke the generator
- Error messages from the CLI are already user-friendly

**Alternatives considered**:
- **Programmatic Java integration via JNI/GraalVM**: Much higher complexity, tight coupling, fragile across Java versions.
- **REST API wrapper**: Adds HTTP server management, port conflicts, CORS — unnecessary for local tool invocation.

### Decision 3: Config-via-parameters (not config file path)

**Choice**: The `generate` tool accepts individual typed parameters (pubName, enableAuthentication, etc.), then the server builds a temporary YAML config file internally.

**Rationale**:
- AI assistants work better with discrete, documented parameters than opaque file paths
- Each parameter has a Zod type, description, and default — fully discoverable
- The server builds the YAML config behind the scenes, keeping the CLI invocation clean

**Alternative considered**:
- **Accept a config file path**: Simpler server code, but forces the AI to create and manage YAML files, which is error-prone and less discoverable.

### Decision 4: Three tools (generate, list-options, validate)

**Choice**: Expose three focused tools rather than one monolithic tool.

**Rationale**:
- `list-options` lets the AI discover capabilities before calling `generate`
- `validate` provides a safe dry-run before committing to generation
- Follows MCP best practice of small, composable tools
- Maps cleanly to the OpenAPI Generator CLI subcommands (`generate`, `config-help`, `validate`)

## Architecture

```
mcp-server/
├── package.json              # npm package with bin entry
├── tsconfig.json
├── src/
│   ├── index.ts              # Entry point: create server, register tools, connect stdio
│   ├── tools/
│   │   ├── generate.ts       # generate tool: spec + options → Dart package
│   │   ├── list-options.ts   # list-options tool: → option descriptions
│   │   └── validate.ts       # validate tool: spec → validation result
│   └── utils/
│       └── runner.ts         # Shells out to java -cp ... OpenAPIGenerator
└── README.md                 # Usage, configuration, examples
```

### Data Flow (generate tool)

```
AI Assistant                     MCP Server                       Java CLI
    │                               │                                │
    ├─ tool_call(generate, {        │                                │
    │    inputSpec, outputDir,      │                                │
    │    pubName, enableAuth...})   │                                │
    │                               ├─ buildConfigYaml(params)       │
    │                               ├─ write /tmp/config.yaml        │
    │                               ├─ execFile("java", [...])  ────►│
    │                               │                                ├─ parse config
    │                               │                                ├─ generate code
    │                               │◄── stdout/stderr ──────────────┤
    │◄── { content: [...] }  ───────┤                                │
    │                               ├─ cleanup /tmp/config.yaml      │
```

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| Java not installed on user's machine | Tool fails at runtime | Clear error message with install instructions; document prerequisite |
| Generator JAR not built | Delays first invocation | Auto-build via `./scripts/build.sh --skip-tests`; clear error if build fails |
| Zod schema drifts from Java CliOptions | Options become stale | Document sync process; future: auto-generate schema from Java |
| Large spec → slow generation | Long tool call | 2-minute timeout; kill process and clean up partial output |
| TypeScript adds Node.js dependency | Extra prerequisite | `npx` handles this transparently; document requirement |
| Concurrent generate calls | Temp file conflicts or output corruption | Use unique temp directories per invocation (e.g., `mkdtemp`) |
| Windows classpath separator differs (`;` vs `:`) | Server fails on Windows | Explicitly unsupported; detect and fail with clear message |

## Migration Plan

No migration needed — this is a new, additive package. Existing workflows (`./scripts/generate-samples.sh`, direct CLI usage) are unaffected.

### Rollout

1. Build and test locally
2. Document in project README
3. Publish to npm as `@dart-acdc/mcp-server`
4. Add to Claude Code configuration examples

## Resolved Questions

1. **npm scope**: Use scoped `@dart-acdc/mcp-server`.
2. **Auto-build**: The server SHALL automatically run `./scripts/build.sh` if the generator JAR is missing, rather than failing with instructions.
3. **Version pinning**: Accept any compatible openapi-generator-cli.jar version. Do not pin to a specific version.
