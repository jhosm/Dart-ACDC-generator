# Dart-ACDC Library

Repository: https://github.com/jhosm/Dart-ACDC

## Overview

**Dart-ACDC** is a zero-config, opinionated HTTP client library for Flutter mobile applications. The acronym stands for **A**uthentication, **C**aching, **D**ebugging, **C**lient.

It serves as the "missing link" between OpenAPI Generator's Dart clients and production-ready Flutter apps, providing essential features that generated clients typically lack.

## Version Information

- **Current Version**: 0.2.0
- **Dart SDK**: ≥3.0.0
- **Flutter**: ≥3.10.0

## Core Philosophy

- **Zero-config**: Sensible defaults that work out of the box
- **Builder pattern**: Progressive disclosure - start simple, add complexity only when needed
- **Type-safe**: Full Dart null safety support
- **Production-ready**: Features like request deduplication, secure storage, and offline support
- **Testable**: Easy mocking support for unit testing

## The Four Pillars (ACDC)

### 1. Authentication (A)

OAuth 2.1-based authentication system with:
- Automatic token injection into requests
- Automatic token refresh (reactive and proactive)
- Secure token storage using OS-level encryption
- Request queuing during token refresh

### 2. Caching (C)

Intelligent HTTP caching with:
- Two-tier architecture (Memory L1 + Disk L2)
- HTTP standard compliance
- User isolation (cache keys include user ID)
- Offline support
- AES-256 encryption for cached data

### 3. Debugging (D)

Environment-aware logging with:
- Configurable log levels
- Automatic sensitive data redaction
- Integration with crash reporting services
- Type-safe error handling

### 4. Client (C)

Enhanced Dio HTTP client with:
- Request deduplication
- Certificate pinning
- Type-safe exceptions
- Full Dio compatibility

## Installation

Add to `pubspec.yaml`:
```yaml
dependencies:
  dart_acdc: ^0.2.0
  dio: ^5.0.0
```

Then run:
```bash
flutter pub get
```

## Quick Start

### Minimal Setup

```dart
final dio = AcdcClientBuilder()
    .withBaseUrl('https://api.example.com')
    .build();

final response = await dio.get('/data');
```

### Basic Configuration

```dart
final dio = AcdcClientBuilder()
    .withBaseUrl('https://api.example.com')
    .withLogging(level: LogLevel.info)
    .withCache(ttl: Duration(hours: 1))
    .build();
```

## Authentication

### Configuration Options

#### Standard OAuth 2.1 Refresh

```dart
final dio = AcdcClientBuilder()
    .withBaseUrl('https://api.example.com')
    .withAuthentication(
      tokenRefreshUrl: 'https://api.example.com/auth/refresh',
      clientId: 'your-client-id',
      clientSecret: 'your-client-secret',
    )
    .build();
```

#### Custom Refresh Logic

For non-standard backends:

```dart
final dio = AcdcClientBuilder()
    .withBaseUrl('https://api.example.com')
    .withCustomRefresh((refreshToken) async {
      // Your custom logic here
      return TokenRefreshResult(
        accessToken: newAccessToken,
        refreshToken: newRefreshToken,
      );
    })
    .build();
```

#### Proactive Refresh

Refresh tokens before they expire:

```dart
.withAuthentication(
  tokenRefreshUrl: 'https://api.example.com/auth/refresh',
  refreshThreshold: Duration(minutes: 5), // Refresh 5 minutes before expiry
)
```

### Token Storage

#### Built-in Secure Storage (Default)

Uses `FlutterSecureStorage` with OS-level encryption:
- **iOS**: Keychain
- **Android**: EncryptedSharedPreferences

```dart
// Default - no configuration needed
final dio = AcdcClientBuilder()
    .withAuthentication(...)
    .build();
```

#### Custom Storage

Implement the `TokenProvider` interface:

```dart
class HiveTokenProvider implements TokenProvider {
  @override
  Future<String?> getAccessToken() async { /* ... */ }

  @override
  Future<String?> getRefreshToken() async { /* ... */ }

  @override
  Future<void> saveTokens(String accessToken, String refreshToken) async { /* ... */ }

  @override
  Future<void> clearTokens() async { /* ... */ }
}

final dio = AcdcClientBuilder()
    .withCustomTokenProvider(HiveTokenProvider())
    .build();
```

### Manual Operations

#### Initialize with Existing Tokens

```dart
final authManager = dio.getAcdcAuthManager();
await authManager.withInitialTokens(
  accessToken: existingAccessToken,
  refreshToken: existingRefreshToken,
);
```

#### Manual Refresh

```dart
await authManager.refreshNow();
```

#### Logout

```dart
// Logout without server-side revocation
await authManager.logout(revokeOnServer: false);

// Logout with server-side revocation
await authManager.logout(
  revokeOnServer: true,
  revokeUrl: 'https://api.example.com/auth/revoke',
);
```

### How It Works

1. **Token Injection**: Automatically adds `Authorization: Bearer {token}` to requests
2. **Reactive Refresh**: When a 401 response is received, automatically refreshes the token
3. **Proactive Refresh**: Checks token expiration before requests and refreshes if needed
4. **Request Queuing**: Pauses pending requests during token refresh, then retries them
5. **Concurrency Control**: Ensures only one refresh operation occurs at a time

## Caching

### Architecture

**Two-tier caching system**:
- **Memory Tier (L1)**: LRU cache in RAM for fast access
- **Persistent Tier (L2)**: Hive-based disk storage for long-term retention

### Configuration

```dart
final dio = AcdcClientBuilder()
    .withCache(
      ttl: Duration(hours: 1),           // Cache expiration
      maxDiskSize: 20 * 1024 * 1024,     // 20 MB disk limit
      maxMemorySize: 5 * 1024 * 1024,    // 5 MB RAM limit
      encrypt: true,                      // AES-256 encryption (default)
      cacheAuthenticatedRequests: true,  // Cache authenticated requests
    )
    .build();
```

### Key Features

#### User Isolation

Cache keys automatically include the user ID (extracted from the auth token), ensuring:
- Cached data doesn't leak between users on shared devices
- Each user has their own isolated cache

#### Stale-While-Revalidate (SWR)

Use `streamRequest` to get cached data immediately while fetching fresh data:

```dart
final stream = dio.streamRequest('/data');

stream.listen((response) {
  final source = response.extra['acdc_source'];

  if (source == 'cache') {
    // Display cached data immediately
    updateUI(response.data);
  } else if (source == 'network') {
    // Update with fresh data when available
    updateUI(response.data);
  }
});
```

Response sources:
- `'cache'` - Data from cache
- `'network'` - Fresh data from network
- `'stale'` - Stale cached data (SWR emission)
- `'fresh'` - Fresh network data (SWR emission)

#### HTTP Standard Compliance

Follows HTTP caching directives:
- `Cache-Control` headers
- `ETag` and `Last-Modified` validation
- Conditional requests (If-None-Match, If-Modified-Since)

### Per-Request Cache Control

```dart
// Bypass cache for a specific request
await dio.get('/data', options: Options(
  extra: {'cache_control': 'no-cache'},
));

// Force network (useful for offline testing)
await dio.get('/data', options: Options(
  extra: {'force_network': true},
));
```

### Disable Caching

```dart
// Disable globally
final dio = AcdcClientBuilder()
    .withCache(enabled: false)
    .build();
```

## Logging and Error Handling

### Logging Configuration

#### Log Levels

- `LogLevel.none` - No logging
- `LogLevel.error` - Only errors
- `LogLevel.warning` - Warnings and errors
- `LogLevel.info` - Info, warnings, and errors
- `LogLevel.debug` - Debug info and above
- `LogLevel.verbose` - All logs including request/response details

```dart
final dio = AcdcClientBuilder()
    .withLogging(level: LogLevel.info)
    .build();
```

#### Sensitive Data Redaction

Automatically redacts sensitive data from logs:
- Passwords
- Tokens
- Authorization headers
- Custom sensitive fields

```dart
final dio = AcdcClientBuilder()
    .withLogging(
      level: LogLevel.verbose,
      sensitiveFields: ['password', 'ssn', 'credit_card'],
    )
    .build();
```

#### Custom Logger Integration

Integrate with crash reporting services:

```dart
class SentryLogDelegate implements AcdcLogDelegate {
  @override
  void log(LogLevel level, String message, [dynamic error, StackTrace? stackTrace]) {
    if (level == LogLevel.error) {
      Sentry.captureException(error, stackTrace: stackTrace);
    }
  }
}

final dio = AcdcClientBuilder()
    .withLogging(
      level: LogLevel.info,
      delegate: SentryLogDelegate(),
    )
    .build();
```

### Error Handling

#### Exception Hierarchy

All errors are normalized into the `AcdcException` hierarchy:

```dart
try {
  final response = await dio.get('/data');
} on AcdcAuthException catch (e) {
  // 401 errors, token refresh failures
  // Action: Redirect to login
  navigateToLogin();
} on AcdcNetworkException catch (e) {
  // Offline, timeouts, DNS issues
  // Action: Show connectivity message
  showOfflineMessage();
} on AcdcServerException catch (e) {
  // 5xx server errors
  // Action: Show error message, maybe retry
  showErrorMessage('Server error, please try again');
} on AcdcClientException catch (e) {
  // 4xx client errors (400, 404, etc.)
  // Action: Show validation errors
  showValidationErrors(e.message);
} on AcdcSecurityException catch (e) {
  // Certificate pinning failures
  // Action: Warn user about security risk
  showSecurityWarning();
} on AcdcException catch (e) {
  // Catch-all for other ACDC errors
  showGenericError();
}
```

#### Exception Types

**AcdcAuthException**:
- Thrown on 401/403 responses
- Token refresh failures
- Invalid credentials

**AcdcNetworkException**:
- No internet connectivity
- Connection timeouts
- DNS resolution failures
- Socket exceptions

**AcdcServerException**:
- 5xx HTTP status codes
- Server-side errors

**AcdcClientException**:
- 4xx HTTP status codes (except 401/403)
- Malformed requests
- Validation errors

**AcdcSecurityException**:
- Certificate pinning failures
- SSL/TLS errors
- Security validation failures

## Request Deduplication

### What It Does

Prevents identical API calls from executing simultaneously, reducing:
- Bandwidth usage
- Server load
- Redundant data processing

### How It Works

When a second request matches an ongoing first request:

1. **Detection**: Identifies the duplicate call
2. **Suppression**: Skips making a redundant network request
3. **Result Sharing**: Returns a Future that resolves with the outcome of the original request

If the first request fails, both requests fail identically.

### What Makes Requests "Identical"

Two requests are considered duplicates when they have the same:
- HTTP method (GET, POST, etc.)
- URL (including query parameters)
- Headers
- Request body
- Response type

### Configuration

**Enabled by default**:

```dart
final dio = AcdcClientBuilder()
    .withDeduplication(enabled: true) // Default
    .build();
```

**Disable globally**:

```dart
final dio = AcdcClientBuilder()
    .withDeduplication(enabled: false)
    .build();
```

### Example

```dart
// These two requests will be deduplicated
final future1 = dio.get('/users/123');
final future2 = dio.get('/users/123');

// Only one network request is made
// Both futures resolve with the same response
```

## Offline Support

### How It Works

The **OfflineInterceptor** operates at the start of the request chain:

1. Detects offline state using `connectivity_plus`
2. Checks for cached responses
3. Returns cached data if available
4. Throws `AcdcNetworkException` if no cache exists (when `failFast` is enabled)

### Configuration

```dart
final dio = AcdcClientBuilder()
    .withOfflineDetection(
      failFast: true,  // Fail immediately when offline with no cache
    )
    .build();
```

**failFast: true** (default):
- Immediately throws `AcdcNetworkException` when offline with no cache
- Avoids connection timeout delays

**failFast: false**:
- Attempts requests even when offline
- Useful when device connectivity checks are unreliable

### Per-Request Bypass

```dart
// Force network request even when offline
await dio.get('/data', options: Options(
  extra: {'force_network': true},
));
```

### Recommendations

**Read operations**:
- Rely on default behavior
- Serve cached content when offline
- Provide good user experience

**Write operations**:
- Fail immediately when offline
- Implement custom local queueing if needed
- Library doesn't provide persistent mutation queues

### Example

```dart
try {
  final response = await dio.get('/users');
  // Data from network or cache
  updateUI(response.data);
} on AcdcNetworkException catch (e) {
  // Offline with no cache
  showOfflineMessage();
}
```

## Certificate Pinning

### What It Is

A security mechanism that defends against Man-in-the-Middle (MITM) attacks by verifying the server presents a specific public key certificate.

### Configuration

```dart
final dio = AcdcClientBuilder()
    .withCertificatePinning(
      config: CertificatePinningConfig(
        pins: {
          'api.example.com': [
            'sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=',
            'sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=', // Backup
          ],
        },
        enforced: true, // Default - fail on mismatch
      ),
    )
    .build();
```

### Modes

#### Enforced Mode (Default)

```dart
.withCertificatePinning(
  config: CertificatePinningConfig(
    pins: {...},
    enforced: true, // Connections fail on mismatch
  ),
)
```

Throws `AcdcSecurityException` if certificate doesn't match.

#### Report-Only Mode

```dart
.withCertificatePinning(
  config: CertificatePinningConfig(
    pins: {...},
    enforced: false, // Connections proceed, but failures are logged
    onPinningFailure: (String host, String? certificate) {
      // Log to monitoring service
      logSecurityEvent('Certificate mismatch for $host');
    },
  ),
)
```

Useful for gradual rollouts and monitoring.

### Obtaining Certificate Hashes

Use OpenSSL to extract SHA-256 hashes of Subject Public Key Info (SPKI):

```bash
# From a PEM certificate file
openssl x509 -in certificate.pem -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  openssl enc -base64

# From a running server
echo | openssl s_client -servername api.example.com \
  -connect api.example.com:443 2>/dev/null | \
  openssl x509 -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  openssl enc -base64
```

### Platform Support

- ✅ iOS
- ✅ Android
- ✅ Desktop (macOS, Windows, Linux)
- ❌ Web (relies on browser-managed certificate validation)

## Integration with OpenAPI Generator

Dart-ACDC is designed to work seamlessly with OpenAPI-generated clients.

### Workflow

1. **Generate Dart client** using OpenAPI Generator:
```bash
openapi-generator generate \
  -g dart-dio \
  -i openapi.yaml \
  -o ./lib/api_client
```

2. **Create configured Dio instance**:
```dart
final dio = AcdcClientBuilder()
    .withBaseUrl('https://api.example.com')
    .withAuthentication(...)
    .withCache(...)
    .withLogging(...)
    .build();
```

3. **Inject into generated API classes**:
```dart
import 'package:your_app/api_client/api.dart';

final userApi = UserApi(dio: dio);
final users = await userApi.getUsers();
```

This provides:
- ✅ Authentication with automatic token refresh
- ✅ Response caching with user isolation
- ✅ Request/response logging with redaction
- ✅ Type-safe error handling
- ✅ Request deduplication
- ✅ Offline support
- ✅ Certificate pinning

## Architecture

### Design Patterns

**Builder Pattern**: `AcdcClientBuilder` allows progressive configuration with immutable builder instances.

**Interceptor Chain**: Rather than modifying Dio, wraps it with pre-configured interceptors handling cross-cutting concerns.

**Composition Over Inheritance**: Builds atop Dio rather than extending it, ensuring compatibility during upgrades.

### Interceptor Chain Order

Critical ordering for proper functionality:

1. **Logging** → Records requests/responses
2. **Error Preparation** → Sets up error handling context
3. **Authentication** → Injects/refreshes tokens
4. **Caching** → Checks cache, stores responses
5. **Custom Logic** → User-defined interceptors
6. **Network Execution** → Actual HTTP request
7. (Reverse order for responses)

### Component Organization

**Authentication (`src/auth`)**:
- Token injection
- Reactive refresh (on 401)
- Proactive refresh (before expiration)
- Concurrency control

**Caching (`src/cache`)**:
- Two-tier architecture (Memory L1 + Disk L2)
- HTTP standards compliance
- User isolation
- Encryption

**Error Handling (`src/exceptions`)**:
- Unified exception hierarchy
- Type-safe error categorization

**Logging (`src/logging`)**:
- Configurable verbosity
- Sensitive data redaction
- External service integration

**Network Info (`src/network_info`)**:
- Connectivity detection
- Offline state management

**Security (`src/security`)**:
- Certificate pinning
- Token storage
- Cache encryption

## Dependencies

### Core Dependencies

- **dio** (^5.4.0) - HTTP client foundation
- **dio_cache_interceptor** (^4.0.5) - Response caching
- **flutter_secure_storage** (^10.0.0) - Secure credential storage
- **connectivity_plus** (^7.0.0) - Network connectivity detection
- **jwt_decoder** (^2.0.1) - JWT token parsing
- **encrypt** (^5.0.3) - Encryption utilities
- **pretty_dio_logger** (^1.3.1) - Request/response logging
- **crypto** - Cryptographic functions
- **path_provider** - File system paths
- **http_cache_file_store** - File-based cache storage

### Development Dependencies

- **mockito** - Mocking for tests
- **http_mock_adapter** - HTTP request mocking
- **build_runner** - Code generation
- **flutter_test** - Testing framework

## Testing

### Test Coverage

91.76% code coverage across the library.

### Mocking Support

Easy to mock for unit testing:

```dart
import 'package:mockito/mockito.dart';
import 'package:http_mock_adapter/http_mock_adapter.dart';

// Mock the Dio instance
final mockDio = MockDio();

// Mock specific requests
when(mockDio.get('/users'))
    .thenAnswer((_) async => Response(
      data: {'users': []},
      statusCode: 200,
    ));
```

## Security Features

### Token Storage

- **iOS**: Keychain (OS-level encryption)
- **Android**: EncryptedSharedPreferences (OS-level encryption)

### Logging Security

- Automatic redaction of `Authorization` headers
- Configurable sensitive field redaction
- Prevents token leakage in logs

### Cache Encryption

- AES-256 encryption for disk cache
- Enabled by default
- User-isolated cache keys

### Certificate Pinning

- SHA-256 hash validation
- MITM attack prevention
- Gradual rollout support with report-only mode

### Minimal Permissions

No special permissions required beyond network access.

## Project Structure

```
dart-acdc/
├── lib/
│   ├── dart_acdc.dart              # Main entry point
│   └── src/
│       ├── auth/                    # Authentication
│       ├── builder/                 # Builder pattern
│       ├── cache/                   # Caching mechanisms
│       ├── cancellation/            # Cancellation tokens
│       ├── exceptions/              # Custom exceptions
│       ├── extensions/              # Dart extensions
│       ├── interceptors/            # Request/response interceptors
│       ├── logging/                 # Logging utilities
│       ├── network_info/            # Network detection
│       └── security/                # Security features
├── test/                            # Test suite (91.76% coverage)
├── doc/                             # Documentation
│   ├── authentication.md
│   ├── caching.md
│   ├── certificate_pinning.md
│   ├── deduplication.md
│   ├── getting_started.md
│   ├── logging_and_errors.md
│   └── offline.md
├── scripts/                         # Development utilities
├── ARCHITECTURE.md
├── CHANGELOG.md
├── pubspec.yaml
└── analysis_options.yaml
```

## Version History

### 0.2.0 (Current)

**Breaking Changes**:
- Switched from `dio_cache_interceptor_file_store` to `http_cache_file_store`
- May require Android configuration adjustments

**Updates**:
- `flutter_secure_storage` → 10.0.0
- `dio_cache_interceptor` → 4.0.5
- `test` → 1.26.3
- `flutter_lints` → 6.0.0

**Improvements**:
- Improved desktop platform compatibility
- Resolved dependency conflicts
- Fixed lint issues

### 0.1.0 (Initial Release)

- Zero-config Dio client
- Built-in authentication
- Caching support
- Logging with redaction
- Error handling

## Key Takeaways

### What Makes Dart-ACDC Special

1. **Production-Ready**: Not just a demo library, designed for real-world apps
2. **OpenAPI Integration**: Perfect companion for generated clients
3. **Zero-Config**: Works out of the box with sensible defaults
4. **Security-First**: OS-level encryption, certificate pinning, data redaction
5. **Offline-First**: Intelligent caching with user isolation
6. **Developer-Friendly**: Type-safe errors, easy mocking, comprehensive docs

### When to Use Dart-ACDC

✅ **Use when**:
- Building Flutter mobile apps with REST APIs
- Using OpenAPI-generated clients
- Need authentication with token refresh
- Want offline support and caching
- Require secure token storage
- Need type-safe error handling

❌ **Don't use when**:
- Building web apps (limited feature support)
- Don't need authentication/caching
- Have very custom requirements that conflict with opinions
- Using GraphQL or other non-REST protocols

### Best Practices

1. **Start simple**: Use minimal configuration, add features as needed
2. **Trust the defaults**: They're designed for common use cases
3. **Use with OpenAPI**: Maximum benefit when paired with generated clients
4. **Handle errors specifically**: Catch specific exception types for better UX
5. **Test offline**: Verify app behavior when network is unavailable
6. **Monitor security**: Use report-only mode when rolling out certificate pinning
7. **Redact sensitive data**: Configure sensitive fields for your specific API

## Resources

- **Repository**: https://github.com/jhosm/Dart-ACDC
- **Documentation**: https://github.com/jhosm/Dart-ACDC/tree/main/doc
- **Pub.dev**: (Package not yet published)
- **Dio Documentation**: https://pub.dev/packages/dio
