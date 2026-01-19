# Tasks: Add Full Dart-ACDC Integration

## 1. Authentication Integration

- [ ] 1.1 Update `api_client.mustache` to call `withAuthentication()` on builder
- [ ] 1.2 Pass `tokenRefreshUrl` from `AuthConfig`
- [ ] 1.3 Support `clientId` and `clientSecret` for OAuth flows
- [ ] 1.4 Support `refreshThreshold` for proactive refresh
- [ ] 1.5 Support custom `TokenProvider` injection
- [ ] 1.6 Support `useSecureStorage` option
- [ ] 1.7 Generate `AuthenticationManager` access helper in README

## 2. Caching Integration

- [ ] 2.1 Update `api_client.mustache` to call `withCache()` on builder
- [ ] 2.2 Pass `ttl` duration from `CacheConfig`
- [ ] 2.3 Pass `maxDiskSize` for cache storage limits
- [ ] 2.4 Support `encrypt` option for AES-256 encryption
- [ ] 2.5 Support `userIsolation` for multi-user cache separation
- [ ] 2.6 Document cache behavior in generated README

## 3. Logging Integration

- [ ] 3.1 Update `api_client.mustache` to call `withLogging()` on builder
- [ ] 3.2 Pass `level` from `LogConfig` (none, error, warning, info, debug, verbose)
- [ ] 3.3 Support `redactSensitiveData` option
- [ ] 3.4 Support custom `Logger` injection (e.g., for Sentry/Crashlytics)
- [ ] 3.5 Document log levels in generated README

## 4. Offline Detection Integration

- [ ] 4.1 Update `api_client.mustache` to call `withOfflineDetection()` on builder
- [ ] 4.2 Support `enabled` flag
- [ ] 4.3 Support `failFast` option for immediate offline exceptions
- [ ] 4.4 Document offline handling patterns in README

## 5. Certificate Pinning Integration

- [ ] 5.1 Update `api_client.mustache` to call `withCertificatePinning()` on builder
- [ ] 5.2 Pass `pins` list from `SecurityConfig`
- [ ] 5.3 Support `reportOnly` mode for gradual rollout
- [ ] 5.4 Document certificate pinning setup in README

## 6. Request Deduplication

- [ ] 6.1 Verify request deduplication is enabled by default in Dart-ACDC
- [ ] 6.2 Document behavior in README (automatic, no configuration needed)

## 7. README Documentation Updates

- [ ] 7.1 Add "Quick Start" section with minimal configuration
- [ ] 7.2 Add "Full Configuration" section with all ACDC options
- [ ] 7.3 Add "Authentication" section with login/logout examples
- [ ] 7.4 Add "Error Handling" section with exception type examples
- [ ] 7.5 Add "Caching" section explaining cache behavior
- [ ] 7.6 Add "Offline Support" section with offline handling patterns
- [ ] 7.7 Add "Testing" section with mock examples

## 8. Codegen Updates

- [ ] 8.1 Add Dart-ACDC types to reserved words if needed
- [ ] 8.2 Ensure proper imports for ACDC types in generated code
- [ ] 8.3 Add any ACDC-specific type mappings

## 9. Verification

- [ ] 9.1 Generate Petstore client with full ACDC config
- [ ] 9.2 Verify `dart analyze` passes
- [ ] 9.3 Manual review of generated ApiClient code
- [ ] 9.4 Verify README is comprehensive and accurate
- [ ] 9.5 Test authentication flow documentation accuracy
