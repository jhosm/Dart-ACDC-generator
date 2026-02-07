# Tasks: Add Configurable Generator Options

## 1. Package Metadata Options

- [x] 1.1 Add `pubName` option (string, required)
- [x] 1.2 Add `pubVersion` option (string, default: "1.0.0")
- [x] 1.3 Add `pubDescription` option (string)
- [x] 1.4 Add `pubAuthor` option (string)
- [x] 1.5 Add `pubHomepage` option (string)

## 2. Feature Toggle Options

- [x] 2.1 Add `enableAuthentication` option (boolean, default: true)
- [x] 2.2 Add `enableCaching` option (boolean, default: true)
- [x] 2.3 Add `enableLogging` option (boolean, default: true)
- [x] 2.4 Add `enableOfflineSupport` option (boolean, default: true)
- [x] 2.5 Add `enableCertificatePinning` option (boolean, default: false)

## 3. Authentication Options

- [x] 3.1 Add `defaultTokenRefreshUrl` option (string)
- [x] 3.2 Add `useSecureTokenStorage` option (boolean, default: true)
- [x] 3.3 Add `refreshThresholdMinutes` option (integer, default: 5)

## 4. Caching Options

- [x] 4.1 Add `defaultCacheTtlHours` option (integer, default: 1)
- [x] 4.2 Add `cacheDiskSizeMb` option (integer, default: 20)
- [x] 4.3 Add `encryptCache` option (boolean, default: true)
- [x] 4.4 Add `enableUserCacheIsolation` option (boolean, default: true)

## 5. Logging Options

- [x] 5.1 Add `defaultLogLevel` option (enum: none/error/warning/info/debug/verbose, default: info)
- [x] 5.2 Add `redactSensitiveData` option (boolean, default: true)

## 6. Code Generation Options

- [x] 6.1 Add `serializationLibrary` option (enum: json_serializable/freezed, default: json_serializable)
- [x] 6.2 Add `generateInterfaces` option (boolean, default: true)
- [x] 6.3 Add `dataSourceSuffix` option (string, default: "RemoteDataSource")
- [x] 6.4 Add `generateBarrelExports` option (boolean, default: true)

## 7. Codegen Class Implementation

- [x] 7.1 Add all options using `cliOptions.add(CliOption...)` *(5 package metadata + 5 feature toggle options registered)*
- [x] 7.2 Implement `processOpts()` to read and validate options *(with smart defaults from OpenAPI spec + convertToBoolean helper)*
- [x] 7.3 Store options in additionalProperties for template access *(package metadata + feature toggles as Boolean objects)*
- [x] 7.4 Add validation for required options (pubName) *(smart default from info.title ensures never empty)*

## 8. Template Updates

- [x] 8.1 Add conditional blocks for `enableAuthentication`
- [x] 8.2 Add conditional blocks for `enableCaching`
- [x] 8.3 Add conditional blocks for `enableLogging`
- [x] 8.4 Add conditional blocks for `enableOfflineSupport`
- [x] 8.5 Add conditional blocks for `enableCertificatePinning`
- [x] 8.6 Add conditional for interface generation
- [x] 8.7 Use `dataSourceSuffix` in naming

## 9. Configuration File Support

- [x] 9.1 Create example YAML config file
- [x] 9.2 Document all options in config file format
- [x] 9.3 Create config file for minimal setup
- [x] 9.4 Create config file for full ACDC setup

## 10. Documentation

- [x] 10.1 Document all options in generator README
- [x] 10.2 Add option descriptions in `getHelp()` output
- [x] 10.3 Create usage examples for common configurations
- [x] 10.4 Document default values for all options

## 11. Verification

- [x] 11.1 Generate with minimal options (just pubName)
- [x] 11.2 Generate with all ACDC features disabled
- [x] 11.3 Generate with all ACDC features enabled (defaults)
- [x] 11.4 Generate with custom log level and cache TTL
- [x] 11.5 Verify option validation error messages
- [x] 11.6 Test with YAML config file
