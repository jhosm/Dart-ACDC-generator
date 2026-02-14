# Change: Add MCP Server for AI Tool Integration

## Why

The Dart-ACDC generator currently requires users to understand Java classpaths, YAML config files, and shell scripts to generate Dart clients. AI coding assistants (Claude Code, Cursor, Windsurf) cannot discover or invoke the generator without manual instruction. An MCP (Model Context Protocol) server exposes the generator as typed, discoverable tools that any MCP-compatible client can call directly, making the generator a first-class citizen in AI-assisted Flutter development workflows.

## What Changes

- Add a new `mcp-server/` TypeScript package at the project root
- Expose three MCP tools: `generate` (produce a Dart client from an OpenAPI spec), `list-options` (show available generator options), and `validate` (check a spec for errors)
- Use stdio transport (Claude Code native, no HTTP server required)
- Wrap the existing Java CLI — no changes to the generator's Java code or Mustache templates
- Publishable as an npm package for easy distribution

## Impact

- Affected specs: none (new capability, no modifications to existing specs)
- Affected code: no changes to `generator/` or `scripts/`; new standalone `mcp-server/` directory
- New dependency: Node.js 20+, `@modelcontextprotocol/server`, `@modelcontextprotocol/node`, `zod`
- Prerequisites: `openapi-generator-cli.jar` must exist; generator JAR is auto-built if missing
- Platform: Unix-like only (macOS, Linux); Windows is not supported
