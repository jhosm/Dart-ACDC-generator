# Tasks: Add Full Dart-ACDC Integration

## 1. Authentication Integration

- [x] 1.1 Update `api_client.mustache` to call `withAuthentication()` on builder
- [x] 1.2 Pass `tokenRefreshUrl` from `AuthConfig`
- [x] 1.3 Support `clientId` and `clientSecret` for OAuth flows
- [x] 1.4 Support `refreshThreshold` for proactive refresh
- [x] 1.5 Support custom `TokenProvider` injection
- [x] 1.6 Support `useSecureStorage` option
- [x] 1.7 Generate `AuthenticationManager` access helper in README

## 2. Caching Integration

- [x] 2.1 Update `api_client.mustache` to call `withCache()` on builder
- [x] 2.2 Pass `ttl` duration from `CacheConfig`
- [x] 2.3 Pass `maxDiskSize` for cache storage limits
- [x] 2.4 Support `encrypt` option for AES-256 encryption
- [x] 2.5 Support `userIsolation` for multi-user cache separation
- [x] 2.6 Document cache behavior in generated README

## 3. Logging Integration

- [x] 3.1 Update `api_client.mustache` to call `withLogging()` on builder
- [x] 3.2 Pass `level` from `LogConfig` (none, error, warning, info, debug, verbose)
- [x] 3.3 Support `redactSensitiveData` option
- [x] 3.4 Support custom `Logger` injection (e.g., for Sentry/Crashlytics)
- [x] 3.5 Document log levels in generated README

## 4. Offline Detection Integration

- [x] 4.1 Update `api_client.mustache` to call `withOfflineDetection()` on builder
- [x] 4.2 Support `enabled` flag
- [x] 4.3 Support `failFast` option for immediate offline exceptions
- [x] 4.4 Document offline handling patterns in README

## 5. Certificate Pinning Integration

- [x] 5.1 Update `api_client.mustache` to call `withCertificatePinning()` on builder
- [x] 5.2 Pass `pins` list from `SecurityConfig`
- [x] 5.3 Support `reportOnly` mode for gradual rollout
- [x] 5.4 Document certificate pinning setup in README

## 6. Request Deduplication

- [x] 6.1 Verify request deduplication is enabled by default in Dart-ACDC
- [x] 6.2 Document behavior in README (automatic, no configuration needed)

## 7. README Documentation Updates

- [x] 7.1 Add "Quick Start" section with minimal configuration
- [x] 7.2 Add "Full Configuration" section with all ACDC options
- [x] 7.3 Add "Authentication" section with login/logout examples
- [x] 7.4 Add "Error Handling" section with exception type examples
- [x] 7.5 Add "Caching" section explaining cache behavior
- [x] 7.6 Add "Offline Support" section with offline handling patterns
- [x] 7.7 Add "Testing" section with mock examples

## 8. Codegen Updates

- [x] 8.1 Add Dart-ACDC types to reserved words if needed
- [x] 8.2 Ensure proper imports for ACDC types in generated code
- [x] 8.3 Add any ACDC-specific type mappings

## 9. Verification

- [x] 9.1 Generate Petstore client with full ACDC config
- [x] 9.2 Verify `dart analyze` passes
- [x] 9.3 Manual review of generated ApiClient code
- [x] 9.4 Verify README is comprehensive and accurate
- [x] 9.5 Test authentication flow documentation accuracy
