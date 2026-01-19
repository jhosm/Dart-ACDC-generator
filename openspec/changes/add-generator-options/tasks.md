# Tasks: Add Configurable Generator Options

## 1. Package Metadata Options

- [ ] 1.1 Add `pubName` option (string, required)
- [ ] 1.2 Add `pubVersion` option (string, default: "1.0.0")
- [ ] 1.3 Add `pubDescription` option (string)
- [ ] 1.4 Add `pubAuthor` option (string)
- [ ] 1.5 Add `pubHomepage` option (string)

## 2. Feature Toggle Options

- [ ] 2.1 Add `enableAuthentication` option (boolean, default: true)
- [ ] 2.2 Add `enableCaching` option (boolean, default: true)
- [ ] 2.3 Add `enableLogging` option (boolean, default: true)
- [ ] 2.4 Add `enableOfflineSupport` option (boolean, default: true)
- [ ] 2.5 Add `enableCertificatePinning` option (boolean, default: false)

## 3. Authentication Options

- [ ] 3.1 Add `defaultTokenRefreshUrl` option (string)
- [ ] 3.2 Add `useSecureTokenStorage` option (boolean, default: true)
- [ ] 3.3 Add `refreshThresholdMinutes` option (integer, default: 5)

## 4. Caching Options

- [ ] 4.1 Add `defaultCacheTtlHours` option (integer, default: 1)
- [ ] 4.2 Add `cacheDiskSizeMb` option (integer, default: 20)
- [ ] 4.3 Add `encryptCache` option (boolean, default: true)
- [ ] 4.4 Add `enableUserCacheIsolation` option (boolean, default: true)

## 5. Logging Options

- [ ] 5.1 Add `defaultLogLevel` option (enum: none/error/warning/info/debug/verbose, default: info)
- [ ] 5.2 Add `redactSensitiveData` option (boolean, default: true)

## 6. Code Generation Options

- [ ] 6.1 Add `serializationLibrary` option (enum: json_serializable/freezed, default: json_serializable)
- [ ] 6.2 Add `generateInterfaces` option (boolean, default: true)
- [ ] 6.3 Add `dataSourceSuffix` option (string, default: "RemoteDataSource")
- [ ] 6.4 Add `generateBarrelExports` option (boolean, default: true)

## 7. Codegen Class Implementation

- [ ] 7.1 Add all options using `cliOptions.add(CliOption...)`
- [ ] 7.2 Implement `processOpts()` to read and validate options
- [ ] 7.3 Store options in additionalProperties for template access
- [ ] 7.4 Add validation for required options (pubName)

## 8. Template Updates

- [ ] 8.1 Add conditional blocks for `enableAuthentication`
- [ ] 8.2 Add conditional blocks for `enableCaching`
- [ ] 8.3 Add conditional blocks for `enableLogging`
- [ ] 8.4 Add conditional blocks for `enableOfflineSupport`
- [ ] 8.5 Add conditional blocks for `enableCertificatePinning`
- [ ] 8.6 Add conditional for interface generation
- [ ] 8.7 Use `dataSourceSuffix` in naming

## 9. Configuration File Support

- [ ] 9.1 Create example YAML config file
- [ ] 9.2 Document all options in config file format
- [ ] 9.3 Create config file for minimal setup
- [ ] 9.4 Create config file for full ACDC setup

## 10. Documentation

- [ ] 10.1 Document all options in generator README
- [ ] 10.2 Add option descriptions in `getHelp()` output
- [ ] 10.3 Create usage examples for common configurations
- [ ] 10.4 Document default values for all options

## 11. Verification

- [ ] 11.1 Generate with minimal options (just pubName)
- [ ] 11.2 Generate with all ACDC features disabled
- [ ] 11.3 Generate with all ACDC features enabled (defaults)
- [ ] 11.4 Generate with custom log level and cache TTL
- [ ] 11.5 Verify option validation error messages
- [ ] 11.6 Test with YAML config file
