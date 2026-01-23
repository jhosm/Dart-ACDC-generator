# How to Use Dart-ACDC Generator

A step-by-step guide for generating production-ready Dart API clients from OpenAPI specifications.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Step-by-Step Guide](#step-by-step-guide)
  - [1. Generate the API Client](#1-generate-the-api-client)
  - [2. Add to Your Flutter Project](#2-add-to-your-flutter-project)
  - [3. Run Build Runner](#3-run-build-runner)
  - [4. Initialize the Client](#4-initialize-the-client)
  - [5. Make API Calls](#5-make-api-calls)
- [Configuration Options](#configuration-options)
  - [Minimal Configuration](#minimal-configuration)
  - [Full Configuration](#full-configuration)
  - [Environment-Specific Configurations](#environment-specific-configurations)
- [Authentication](#authentication)
  - [After Login](#after-login)
  - [Logout](#logout)
  - [Custom Token Provider](#custom-token-provider)
- [Error Handling](#error-handling)
- [Caching](#caching)
- [Offline Support](#offline-support)
- [Testing](#testing)
- [Advanced Topics](#advanced-topics)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before you begin, ensure you have:

- **Java 21+** installed (required to run OpenAPI Generator)
- **Dart SDK 3.0+** or **Flutter 3.10+**
- An **OpenAPI specification** file (YAML or JSON) for your API

### Install OpenAPI Generator

Choose one of these methods:

```bash
# Option 1: Homebrew (macOS)
brew install openapi-generator

# Option 2: NPM (cross-platform)
npm install -g @openapitools/openapi-generator-cli

# Option 3: Download JAR directly
wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/7.0.0/openapi-generator-cli-7.0.0.jar -O openapi-generator-cli.jar
```

---

## Quick Start

For the impatient, here's the minimal workflow:

```bash
# 1. Generate the client
openapi-generator generate \
  -g dart-acdc-generator \
  -i your-api-spec.yaml \
  -o ./packages/my_api_client \
  --additional-properties pubName=my_api_client

# 2. Add to your pubspec.yaml
# dependencies:
#   my_api_client:
#     path: ./packages/my_api_client

# 3. Run build_runner
cd packages/my_api_client && dart run build_runner build

# 4. Use in your code
```

```dart
import 'package:my_api_client/my_api_client.dart';

void main() async {
  final dio = ApiClient.createDio(
    AcdcConfig(baseUrl: 'https://api.example.com'),
  );

  final userApi = UserRemoteDataSourceImpl(dio);
  final user = await userApi.getUser('123');
  print(user.email);
}
```

---

## Step-by-Step Guide

### 1. Generate the API Client

Run the generator with your OpenAPI spec:

```bash
openapi-generator generate \
  -g dart-acdc-generator \
  -i path/to/your/openapi.yaml \
  -o ./packages/my_api_client \
  --additional-properties pubName=my_api_client,pubVersion=1.0.0
```

#### Common Generator Options

| Option | Description | Default |
|--------|-------------|---------|
| `pubName` | Package name | `openapi` |
| `pubVersion` | Package version | `1.0.0` |
| `pubDescription` | Package description | Generated from spec |
| `pubAuthor` | Package author | - |
| `enableAuthentication` | Include auth support | `true` |
| `enableCaching` | Include caching support | `true` |
| `enableOfflineSupport` | Include offline detection | `true` |
| `useJsonSerializable` | Use json_serializable | `true` |

#### Using a Configuration File

For complex setups, create a config file (`generator-config.yaml`):

```yaml
generatorName: dart-acdc-generator
inputSpec: ./openapi.yaml
outputDir: ./packages/my_api_client
additionalProperties:
  pubName: my_api_client
  pubVersion: 1.0.0
  pubDescription: "My API Client"
  pubAuthor: "Your Name"
  enableAuthentication: true
  enableCaching: true
  enableOfflineSupport: true
```

Then run:

```bash
openapi-generator generate -c generator-config.yaml
```

### 2. Add to Your Flutter Project

The generated package is a standalone Dart package. Add it to your project's `pubspec.yaml`:

```yaml
dependencies:
  flutter:
    sdk: flutter

  # Local path dependency
  my_api_client:
    path: ./packages/my_api_client
```

Then run:

```bash
flutter pub get
```

### 3. Run Build Runner

The generated models use `json_serializable`. Generate the serialization code:

```bash
cd packages/my_api_client
dart run build_runner build --delete-conflicting-outputs
```

> **Tip**: Add this as a script in your root `package.json` or create a shell script for easy re-generation.

### 4. Initialize the Client

Create a configured Dio instance using the `ApiClient` factory:

```dart
import 'package:dio/dio.dart';
import 'package:my_api_client/my_api_client.dart';

class ApiService {
  late final Dio _dio;
  late final UserRemoteDataSource _userApi;
  late final ProductRemoteDataSource _productApi;

  void initialize() {
    _dio = ApiClient.createDio(
      AcdcConfig(
        baseUrl: 'https://api.example.com',
        auth: AuthConfig(
          tokenRefreshUrl: 'https://api.example.com/auth/refresh',
        ),
        cache: CacheConfig(
          ttl: Duration(hours: 1),
        ),
        log: LogConfig(
          level: LogLevel.debug,
        ),
      ),
    );

    // Create data source instances
    _userApi = UserRemoteDataSourceImpl(_dio);
    _productApi = ProductRemoteDataSourceImpl(_dio);
  }
}
```

### 5. Make API Calls

Use the generated data sources to interact with your API:

```dart
// Get a single resource
final user = await userApi.getUser('user-123');
print('Hello, ${user.displayName}');

// List resources with pagination
final users = await userApi.listUsers(page: 1, limit: 20);
for (final user in users) {
  print(user.email);
}

// Create a resource
final newUser = User(
  id: '', // Will be assigned by server
  email: 'new@example.com',
  displayName: 'New User',
  createdAt: DateTime.now(),
);
final createdUser = await userApi.createUser(newUser);

// Update a resource
final updatedUser = await userApi.updateUser('user-123', user);

// Partial update
await userApi.patchUser('user-123', {'displayName': 'Updated Name'});

// Delete a resource
await userApi.deleteUser('user-123');
```

---

## Configuration Options

### Minimal Configuration

For simple APIs without authentication:

```dart
final dio = ApiClient.createDio(
  AcdcConfig(baseUrl: 'https://api.example.com'),
);
```

### Full Configuration

Enable all Dart-ACDC features:

```dart
final dio = ApiClient.createDio(
  AcdcConfig(
    baseUrl: 'https://api.example.com',

    // Authentication with OAuth 2.1 token refresh
    auth: AuthConfig(
      tokenRefreshUrl: 'https://api.example.com/auth/refresh',
      clientId: 'my-flutter-app',
      clientSecret: 'optional-secret',
      refreshThreshold: Duration(minutes: 5),
      useSecureStorage: true, // Use Keychain/EncryptedSharedPreferences
    ),

    // Two-tier caching (Memory + Disk)
    cache: CacheConfig(
      ttl: Duration(hours: 2),
      maxDiskSizeBytes: 50 * 1024 * 1024, // 50 MB
      encrypt: true,             // AES-256 encryption
      enableUserIsolation: true, // Separate cache per user
    ),

    // Logging
    log: LogConfig(
      level: LogLevel.debug,     // none, error, warning, info, debug, verbose
      redactSensitiveData: true, // Mask tokens, passwords in logs
    ),

    // Offline detection
    offline: OfflineConfig(
      enabled: true,
      failFast: false, // false = queue requests, true = immediate failure
    ),

    // Certificate pinning
    security: SecurityConfig(
      certificatePins: [
        'sha256/AAAA...',
        'sha256/BBBB...',
      ],
      reportOnlyMode: false,
    ),
  ),
);
```

### Environment-Specific Configurations

Create configuration presets for different environments:

```dart
class AppConfig {
  static AcdcConfig get development => AcdcConfig(
    baseUrl: 'https://dev-api.example.com',
    log: LogConfig(level: LogLevel.debug),
    cache: CacheConfig(ttl: Duration(minutes: 5)),
  );

  static AcdcConfig get staging => AcdcConfig(
    baseUrl: 'https://staging-api.example.com',
    auth: AuthConfig(
      tokenRefreshUrl: 'https://staging-api.example.com/auth/refresh',
    ),
    log: LogConfig(level: LogLevel.info),
    cache: CacheConfig(ttl: Duration(hours: 1)),
  );

  static AcdcConfig get production => AcdcConfig(
    baseUrl: 'https://api.example.com',
    auth: AuthConfig(
      tokenRefreshUrl: 'https://api.example.com/auth/refresh',
      useSecureStorage: true,
    ),
    cache: CacheConfig(
      ttl: Duration(hours: 2),
      encrypt: true,
    ),
    log: LogConfig(
      level: LogLevel.warning,
      redactSensitiveData: true,
    ),
    offline: OfflineConfig(enabled: true),
    security: SecurityConfig(
      certificatePins: ['sha256/...'],
    ),
  );
}

// Usage
final dio = ApiClient.createDio(
  kDebugMode ? AppConfig.development : AppConfig.production,
);
```

---

## Authentication

The generated client includes full OAuth 2.1 support with automatic token refresh.

> **Note**: The examples below use Dart-ACDC extension methods on `Dio` (e.g., `getAcdcAuthManager()`, `clearAcdcCache()`). These are provided by the `dart_acdc` package and are available when you import `package:dart_acdc/dart_acdc.dart`.

### After Login

When your user logs in and you receive tokens, store them in ACDC:

```dart
// Get the auth manager from the Dio instance
final authManager = dio.getAcdcAuthManager();

// Store tokens after successful login
await authManager.withInitialTokens(
  accessToken: loginResponse.accessToken,
  refreshToken: loginResponse.refreshToken,
);

// Tokens are now automatically:
// - Attached to all subsequent requests
// - Refreshed when near expiration
// - Stored securely (if useSecureStorage: true)
```

### Logout

Clear tokens and optionally revoke them on the server:

```dart
final authManager = dio.getAcdcAuthManager();

// Logout (clears local tokens)
await authManager.logout();

// Or logout and revoke on server
await authManager.logout(revokeOnServer: true);
```

### Custom Token Provider

For advanced scenarios (e.g., biometric auth, custom storage):

```dart
class MyTokenProvider implements TokenProvider {
  @override
  Future<String?> getAccessToken() async {
    // Custom logic to retrieve access token
  }

  @override
  Future<String?> getRefreshToken() async {
    // Custom logic to retrieve refresh token
  }

  @override
  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    // Custom logic to save tokens
  }

  @override
  Future<void> clearTokens() async {
    // Custom logic to clear tokens
  }
}

// Use custom provider
final dio = ApiClient.createDio(
  AcdcConfig(
    baseUrl: 'https://api.example.com',
    auth: AuthConfig(
      tokenRefreshUrl: 'https://api.example.com/auth/refresh',
      customTokenProvider: MyTokenProvider(),
    ),
  ),
);
```

---

## Error Handling

All API methods throw typed exceptions. Handle them appropriately:

```dart
try {
  final user = await userApi.getUser('123');
  showUserProfile(user);
} on AcdcAuthException catch (e) {
  // 401/403 errors, token refresh failures
  // User needs to re-authenticate
  navigateToLogin();
} on AcdcNetworkException catch (e) {
  // Offline, timeout, DNS failure
  showOfflineMessage('Please check your connection');
} on AcdcServerException catch (e) {
  // 5xx server errors
  showError('Server is temporarily unavailable');
  reportToSentry(e);
} on AcdcClientException catch (e) {
  // 4xx client errors (400, 404, 422, etc.)
  showError('Request failed: ${e.message}');
} on AcdcSecurityException catch (e) {
  // Certificate pinning failure (possible MITM attack)
  showSecurityAlert('Connection may not be secure');
  reportSecurityIncident(e);
}
```

### Global Error Handler

Set up a centralized error handler:

```dart
class ApiErrorHandler {
  static void handle(Object error, {VoidCallback? onAuthFailure}) {
    if (error is AcdcAuthException) {
      onAuthFailure?.call();
    } else if (error is AcdcNetworkException) {
      showToast('No internet connection');
    } else if (error is AcdcServerException) {
      showToast('Server error. Please try again later.');
    } else if (error is AcdcClientException) {
      showToast(error.message ?? 'Request failed');
    } else if (error is AcdcSecurityException) {
      // Log and alert - potential security issue
      logSecurityEvent(error);
    } else {
      showToast('An unexpected error occurred');
    }
  }
}

// Usage
try {
  await userApi.deleteUser('123');
} catch (e) {
  ApiErrorHandler.handle(e, onAuthFailure: () => navigateToLogin());
}
```

---

## Caching

When caching is enabled, GET requests are automatically cached.

### Cache Behavior

- **Memory cache**: Fast, cleared when app closes
- **Disk cache**: Persistent, encrypted (optional), respects TTL
- **User isolation**: Each user has separate cache (prevents data leaks)

### Manual Cache Control

Cache control is handled at the Dio level, not through the RemoteDataSource methods:

```dart
// Clear all cache
await dio.clearAcdcCache();

// Clear cache for specific user (on logout)
await dio.clearAcdcCacheForUser(userId);
```

> **Note**: If you need per-request cache control (e.g., force refresh), you'll need to configure this at the Dio interceptor level or use Dio directly for those specific calls.

---

## Offline Support

When offline support is enabled, the client detects network status and responds accordingly.

### Fail-Fast Mode

```dart
offline: OfflineConfig(
  enabled: true,
  failFast: true, // Immediately throws AcdcNetworkException when offline
),
```

### Queue Mode (Default)

```dart
offline: OfflineConfig(
  enabled: true,
  failFast: false, // Queues requests, executes when back online
),
```

### Check Network Status

```dart
final isOnline = await dio.isAcdcOnline();

if (!isOnline) {
  showOfflineBanner();
}
```

---

## Testing

The generated code separates interfaces from implementations, making testing straightforward.

### Mock the Interface

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:my_api_client/my_api_client.dart';

import 'user_repository_test.mocks.dart';

@GenerateMocks([UserRemoteDataSource])
void main() {
  late MockUserRemoteDataSource mockUserApi;
  late UserRepository userRepository;

  setUp(() {
    mockUserApi = MockUserRemoteDataSource();
    userRepository = UserRepository(mockUserApi);
  });

  test('getUser returns user on success', () async {
    // Arrange
    final expectedUser = User(
      id: '123',
      email: 'test@example.com',
      createdAt: DateTime.now(),
    );
    when(mockUserApi.getUser('123'))
        .thenAnswer((_) async => expectedUser);

    // Act
    final result = await userRepository.getUser('123');

    // Assert
    expect(result, expectedUser);
    verify(mockUserApi.getUser('123')).called(1);
  });

  test('getUser throws on network error', () async {
    // Arrange
    when(mockUserApi.getUser(any))
        .thenThrow(AcdcNetworkException('No connection'));

    // Act & Assert
    expect(
      () => userRepository.getUser('123'),
      throwsA(isA<AcdcNetworkException>()),
    );
  });
}
```

### Run build_runner for mocks

```bash
dart run build_runner build
```

---

## Advanced Topics

### Dependency Injection

Use your preferred DI solution (get_it, riverpod, provider):

```dart
// Using get_it
final getIt = GetIt.instance;

void setupDependencies() {
  // Register Dio
  getIt.registerSingleton<Dio>(
    ApiClient.createDio(AppConfig.production),
  );

  // Register data sources
  getIt.registerLazySingleton<UserRemoteDataSource>(
    () => UserRemoteDataSourceImpl(getIt<Dio>()),
  );

  getIt.registerLazySingleton<ProductRemoteDataSource>(
    () => ProductRemoteDataSourceImpl(getIt<Dio>()),
  );
}

// Usage
final userApi = getIt<UserRemoteDataSource>();
```

### Request Interceptors

Add custom interceptors for logging, analytics, etc.:

```dart
final dio = ApiClient.createDio(config);

// Add custom interceptor
dio.interceptors.add(
  InterceptorsWrapper(
    onRequest: (options, handler) {
      // Add custom headers
      options.headers['X-App-Version'] = '1.0.0';
      handler.next(options);
    },
    onResponse: (response, handler) {
      // Track analytics
      analytics.trackApiCall(response.requestOptions.path);
      handler.next(response);
    },
    onError: (error, handler) {
      // Log errors
      logger.error('API Error', error: error);
      handler.next(error);
    },
  ),
);
```

### Regenerating After API Changes

When your API spec changes:

```bash
# 1. Regenerate the client (overwrites generated files)
openapi-generator generate -c generator-config.yaml

# 2. Regenerate serialization code
cd packages/my_api_client && dart run build_runner build --delete-conflicting-outputs

# 3. Fix any compilation errors in your code
flutter analyze
```

---

## Troubleshooting

### Common Issues

#### "dart_acdc not found"

Ensure the generated `pubspec.yaml` includes dart_acdc:

```yaml
dependencies:
  dart_acdc: ^0.2.0
  dio: ^5.0.0
```

Run `flutter pub get`.

#### "Missing *.g.dart files"

Run build_runner:

```bash
cd packages/my_api_client
dart run build_runner build --delete-conflicting-outputs
```

#### "Certificate pinning failures in debug"

Disable certificate pinning for development:

```dart
final dio = ApiClient.createDio(
  AcdcConfig(
    baseUrl: 'https://api.example.com',
    security: kDebugMode ? null : SecurityConfig(
      certificatePins: ['sha256/...'],
    ),
  ),
);
```

#### "Token refresh not working"

Verify:
1. `tokenRefreshUrl` is correct
2. Refresh token is valid and not expired
3. Server returns new tokens in expected format

```dart
auth: AuthConfig(
  tokenRefreshUrl: 'https://api.example.com/auth/refresh', // Check this URL
),
```

### Getting Help

- **Documentation**: Check the `README.md` in your generated package
- **Issues**: Report bugs at [GitHub Issues](https://github.com/your-org/dart-acdc-generator-generator/issues)
- **Dart-ACDC Docs**: https://github.com/jhosm/Dart-ACDC/tree/main/doc

---

## Summary

1. **Generate** → `openapi-generator generate -g dart-acdc-generator ...`
2. **Add dependency** → `pubspec.yaml`
3. **Build** → `dart run build_runner build`
4. **Configure** → `ApiClient.createDio(AcdcConfig(...))`
5. **Use** → `UserRemoteDataSourceImpl(dio).getUser('123')`
6. **Handle errors** → Try/catch with typed `AcdcException`s

You now have a production-ready API client with authentication, caching, offline support, and proper error handling—all generated automatically from your OpenAPI spec.
