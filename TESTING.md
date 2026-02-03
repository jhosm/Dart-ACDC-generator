# Testing Guide

This document describes the testing strategy for the Dart-ACDC generator.

## Test Layers

The generator has three layers of testing:

### 1. Java Unit Tests
**Location:** `generator/src/test/java/org/openapitools/codegen/languages/DartAcdcGeneratorTest.java`

**Coverage:**
- CLI option registration and defaults
- Package name sanitization
- Type mappings and conversions
- Enum handling
- Name collision resolution
- Option validation (range checks, enum values)
- Edge cases and error handling

**Run:** `mvn test`

**Test Count:** 35+ tests

### 2. Integration Tests
**Location:** `scripts/verify-cli-options.sh`

**Coverage:**
- End-to-end verification that CLI options affect generated code
- Default values in generated config files
- Custom values in generated config files
- Generated code compiles and builds successfully

**Run:** `./scripts/verify-cli-options.sh`

**Test Count:** 18 checks (8 default + 9 custom + 1 build)

**What it does:**
1. Generates sample with default CLI option values
2. Verifies generated config files contain default values
3. Generates sample with custom CLI option values
4. Verifies generated config files contain custom values
5. Builds and tests the custom config sample

### 3. Generated Dart Tests
**Location:** `samples/generated/*/test/*_test.dart`

**Coverage:**
- API method functionality (GET, POST, DELETE, etc.)
- JSON serialization/deserialization
- Error handling (404, 500, network errors)
- Path and query parameter substitution
- Mock request/response behavior

**Run:** `./scripts/test-samples.sh`

**Test Count:** 134+ tests across all samples

## Running All Tests

```bash
# 1. Build the generator
mvn clean package

# 2. Run Java unit tests
mvn test

# 3. Run CLI options integration tests
./scripts/verify-cli-options.sh

# 4. Run generated Dart tests
./scripts/test-samples.sh
```

## CLI Options Integration Testing

The integration tests verify that CLI options correctly propagate from the config YAML to the generated Dart code.

### Tested CLI Options

**Cache Configuration:**
- `defaultCacheTtlHours` (default: 1, tested: 24)
- `encryptCache` (default: true, tested: false)
- `cacheDiskSizeMb` (default: 20, tested: 50)
- `enableUserCacheIsolation` (default: true, tested: false)

**Logging Configuration:**
- `defaultLogLevel` (default: "info", tested: "debug")
- `redactSensitiveData` (default: true, tested: false)

**Authentication Configuration:**
- `refreshThresholdMinutes` (default: 5, tested: 10)
- `useSecureTokenStorage` (default: true, tested: false)
- `defaultTokenRefreshUrl` (default: none, tested: custom URL)

### Sample Configs

**Default values:** `bin/configs/dart-acdc-petstore.yaml`
- Uses all default CLI option values
- Generated code: `samples/generated/petstore/`

**Custom values:** `bin/configs/dart-acdc-custom-config-test.yaml`
- Uses custom CLI option values (different from defaults)
- Generated code: `samples/generated/custom-config-test/`

## Test Coverage

| Layer | What | How | Count |
|-------|------|-----|-------|
| Java Unit | Option processing | JUnit 5 | 35+ |
| Integration | Option → Code | Shell script + grep | 18 |
| Dart Runtime | API functionality | Dart test | 134+ |

## Adding New Tests

### Java Unit Test
```java
@Test
@DisplayName("CLI options: myNewOption should have default value")
void testCliOptions_MyNewOption() {
    Optional<CliOption> option = generator.cliOptions().stream()
            .filter(opt -> "myNewOption".equals(opt.getOpt()))
            .findFirst();
    assertTrue(option.isPresent());
    assertEquals("expectedDefault", option.get().getDefault());
}
```

### Integration Test
Edit `scripts/verify-cli-options.sh`:
```bash
check_file_contains \
    "samples/generated/custom-config-test/lib/config/my_config.dart" \
    "myField = customValue" \
    "Custom myField is customValue (not default)"
```

### Generated Dart Test
Templates automatically generate tests. To add config tests, create:
`generator/src/main/resources/dart-acdc/config_test.mustache`

## Continuous Integration

These tests should be run in CI:
```bash
# .github/workflows/test.yml
- run: mvn clean package
- run: mvn test
- run: ./scripts/verify-cli-options.sh
- run: ./scripts/test-samples.sh
```

## Test Philosophy

**Java tests** verify the transformation logic is correct.
**Integration tests** verify the transformation actually happens.
**Dart tests** verify the generated code works at runtime.

All three layers are necessary for confidence in the generator.
