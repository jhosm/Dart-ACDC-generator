## 1. Project Setup
- [ ] 1.1 Create `mcp-server/` directory at project root
- [ ] 1.2 Initialize `package.json` with name `@dart-acdc/mcp-server`, version, bin entry, `engines` field (`node >= 20`), and dependencies (`@modelcontextprotocol/server`, `@modelcontextprotocol/node`, `zod`)
- [ ] 1.3 Configure `tsconfig.json` targeting ES2022, Node.js module resolution
- [ ] 1.4 Add `.gitignore` for `node_modules/`, `dist/`, and `*.tsbuildinfo`
- [ ] 1.5 Add ESLint and Prettier configuration for consistent code style
- [ ] 1.6 Verify `npm install` and `npm run build` succeed with empty `src/index.ts`

## 2. CLI Runner Utility
- [ ] 2.1 Implement `src/utils/runner.ts` — resolves JAR paths from env vars or conventions, shells out to `java -cp ... OpenAPIGenerator`
- [ ] 2.2 Implement JAR discovery: glob for `dart-acdc-generator-*.jar` in `generator/target/`, select most recently modified if multiple found
- [ ] 2.3 Implement auto-build: when generator JAR is missing, automatically run `./scripts/build.sh --skip-tests` and retry discovery
- [ ] 2.4 Add error handling: missing CLI JAR (with download URL), missing Java runtime, CLI timeout (2 min, kill process), auto-build failure (include build output), auto-build success but JAR still missing, missing Maven, no configuration provided (list required env vars)
- [ ] 2.5 Add Windows detection: fail early with clear "not supported" message
- [ ] 2.6 Ensure unique temp directory per invocation (`mkdtemp`) for concurrent safety
- [ ] 2.7 Write unit tests: missing JAR triggers auto-build, auto-build failure, auto-build success but JAR missing, multiple JARs selects newest, successful invocation mock, timeout kills process, missing Java detection, Windows detection, no env vars error

## 3. Generate Tool
- [ ] 3.1 Implement `src/tools/generate.ts` with Zod input schema matching all generator CLI options
- [ ] 3.2 Implement `buildConfigYaml()` to convert typed params into a temporary YAML config file (unique temp dir)
- [ ] 3.3 Resolve relative `inputSpec` and `outputDir` paths against the server's working directory
- [ ] 3.4 Return structured result with output path, next steps (`dart pub get`, `dart run build_runner build`, `dart analyze`), and generator stdout/stderr
- [ ] 3.5 Warn when `outputDir` already contains files
- [ ] 3.6 Clean up temporary config file after invocation (in `finally` block)
- [ ] 3.7 On timeout, clean up partial output from `outputDir`
- [ ] 3.8 Write tests: schema validation, config YAML generation, relative path resolution, output dir exists warning, output dir not writable error, malformed spec error, timeout cleanup, error paths

## 4. List Options Tool
- [ ] 4.1 Implement `src/tools/list-options.ts` — calls `config-help -g dart-acdc`
- [ ] 4.2 Write tests: successful output, missing CLI JAR error, missing generator JAR triggers auto-build

## 5. Validate Tool
- [ ] 5.1 Implement `src/tools/validate.ts` — calls `validate -i <spec>`
- [ ] 5.2 Handle valid spec (`isError` absent), invalid spec (`isError: true` with CLI output verbatim), spec-with-warnings (`isError` absent, warnings in content), non-existent file, non-YAML/JSON file
- [ ] 5.3 Write tests: valid spec, invalid spec, warnings, missing file, binary file input

## 6. Server Entry Point
- [ ] 6.1 Implement `src/index.ts` — create McpServer, register all three tools, connect stdio transport
- [ ] 6.2 Write programmatic integration test: spawn server as child process, send JSON-RPC tool list request over stdin, assert all three tools returned with correct schemas
- [ ] 6.3 Manual smoke test: configure in Claude Code settings, invoke each tool

## 7. Documentation
- [ ] 7.1 Write `mcp-server/README.md` with prerequisites (Java 21+, Maven 3.8+, Node.js 20+), installation, configuration examples (local path and npx), usage, platform support (macOS/Linux only), and troubleshooting
- [ ] 7.2 Update project root `CLAUDE.md` to mention the MCP server under Distribution

## 8. Packaging (parallelizable with 7)
- [ ] 8.1 Add npm `build`, `start`, and `lint` scripts to `package.json`
- [ ] 8.2 Configure `bin` entry for npx execution under `@dart-acdc/mcp-server`
- [ ] 8.3 Add `.npmignore` to exclude test files, source maps, and dev config from published package
- [ ] 8.4 Verify `npx .` works locally as a smoke test
