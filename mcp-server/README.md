# @dart-acdc/mcp-server

MCP (Model Context Protocol) server that exposes the Dart-ACDC generator as typed, discoverable tools for AI coding assistants.

## Prerequisites

- **Java 21+** — required to run the OpenAPI Generator CLI
- **Maven 3.8+** — required to build the generator JAR (auto-built on first use)
- **Node.js 20+** — required to run the MCP server
- **macOS or Linux** — Windows is not currently supported

You also need the OpenAPI Generator CLI JAR. Download it from:

```
https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/7.10.0/openapi-generator-cli-7.10.0.jar
```

Place it in the project root as `openapi-generator-cli.jar`.

## Tools

### `generate`

Generate a Dart-ACDC API client from an OpenAPI specification. Accepts all generator options as typed parameters (package metadata, ACDC feature toggles, auth/cache/logging config, code style).

### `list-options`

List all available configuration options for the dart-acdc generator, including types, descriptions, and default values.

### `validate`

Validate an OpenAPI specification file for compatibility with the dart-acdc generator.

## Configuration

### Claude Code (local path)

Add to your Claude Code MCP settings (`~/.claude/settings.json` or project `.claude/settings.json`):

```json
{
  "mcpServers": {
    "dart-acdc": {
      "command": "node",
      "args": ["/path/to/Dart-ACDC-generator/mcp-server/dist/index.js"],
      "env": {
        "DART_ACDC_PROJECT_ROOT": "/path/to/Dart-ACDC-generator"
      }
    }
  }
}
```

### Claude Code (npx)

```json
{
  "mcpServers": {
    "dart-acdc": {
      "command": "npx",
      "args": ["@dart-acdc/mcp-server"],
      "env": {
        "DART_ACDC_PROJECT_ROOT": "/path/to/Dart-ACDC-generator"
      }
    }
  }
}
```

### Environment Variables

| Variable | Description |
|---|---|
| `DART_ACDC_PROJECT_ROOT` | Path to the Dart-ACDC generator project root. Used to find JARs and build scripts. |
| `OPENAPI_CLI_JAR` | Explicit path to `openapi-generator-cli.jar`. Overrides auto-discovery. |
| `DART_ACDC_GENERATOR_JAR` | Explicit path to `dart-acdc-generator-*.jar`. Overrides auto-discovery. |

If no environment variables are set, the server infers the project root from its own location.

## Auto-Build

When the generator JAR (`dart-acdc-generator-*.jar`) is not found, the server automatically runs `./scripts/build.sh --skip-tests` to build it. This requires Maven to be installed.

## Development

```bash
cd mcp-server

# Install dependencies
npm install

# Build
npm run build

# Run tests
npm test

# Lint
npm run lint
```

## Platform Support

macOS and Linux only. The server detects Windows and exits with a clear error message.
