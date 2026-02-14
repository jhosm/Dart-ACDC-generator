# ADR-001: Generated Code Architecture

**Status**: Accepted
**Date**: 2026-01-19
**Context**: Defining how the Dart-ACDC-generator generator should structure and name generated code

## Context

The Dart-ACDC-generator generator produces Dart API clients from OpenAPI specifications. We need to establish clear conventions for:

1. How generated code is structured
2. Naming conventions that communicate intent
3. Patterns that support testability
4. Error handling strategy
5. Client configuration that exposes full Dart-ACDC capabilities

## Decisions

### 1. Naming Convention: `*RemoteDataSource`

**Decision**: Use the `RemoteDataSource` suffix for API classes.

**Rationale**:
- Makes it explicit that these classes perform remote API calls
- Widely recognized in Flutter/Dart clean architecture
- Clearly distinguishes from local data sources (cache, database)
- Pairs naturally with `LocalDataSource` for cache/DB implementations

**Example**:
```dart
class UserRemoteDataSource { ... }
class ProductRemoteDataSource { ... }
class OrderRemoteDataSource { ... }
```

### 2. Mocking Support: Abstract Interface + Implementation Split

**Decision**: Generate both an abstract interface and a concrete implementation for each RemoteDataSource.

**Rationale**:
- Enables easy mocking in tests without Dio/network dependencies
- Follows dependency inversion principle
- Users can swap implementations (e.g., for different environments)

**Generated Files**:
- `user_remote_data_source.dart` - Abstract interface
- `user_remote_data_source_impl.dart` - Concrete implementation

### 3. Error Handling: Wrap DioException → AcdcException

**Decision**: Every method wraps `DioException` and converts it to typed `AcdcException`.

**Rationale**:
- Provides consistent, typed error handling across all API calls
- Users get semantic exceptions based on error type
- Hides Dio implementation details from consuming code

**Exception Types** (from Dart-ACDC):
| Exception | When Thrown |
|-----------|-------------|
| `AcdcAuthException` | 401/403 errors, token refresh failures |
| `AcdcNetworkException` | Offline, timeouts, DNS failures |
| `AcdcServerException` | 5xx server errors |
| `AcdcClientException` | 4xx client errors (400, 404, 422, etc.) |
| `AcdcSecurityException` | Certificate pinning failures |

**Pattern**:
```dart
Future<User> getUser(String userId) async {
  try {
    final response = await _dio.get('/users/$userId');
    return User.fromJson(response.data as Map<String, dynamic>);
  } on DioException catch (e) {
    throw AcdcException.fromDioException(e);
  }
}
```

### 4. Dependency Injection: Constructor Injection

**Decision**: Pass `Dio` instance via constructor.

**Rationale**:
- Explicit dependencies, no hidden state
- Easy to test (inject mock Dio or mock the interface)
- Supports different Dio configurations per instance

**Pattern**:
```dart
class UserRemoteDataSourceImpl implements UserRemoteDataSource {
  final Dio _dio;

  UserRemoteDataSourceImpl(this._dio);
}
```

### 5. Parameter Handling: Type-Based Conventions

**Decision**: Use consistent parameter conventions based on OpenAPI parameter location.

**Parameter Types**:
| OpenAPI Location | Dart Convention | Example |
|------------------|-----------------|---------|
| Path parameters | Positional required | `String userId` |
| Query parameters (optional) | Named optional | `{int? page}` |
| Query parameters (required) | Named required | `{required String filter}` |
| Header parameters (optional) | Named optional | `{String? acceptLanguage}` |
| Header parameters (required) | Named required | `{required String xRequestId}` |
| Request body (model) | Positional required | `User user` |
| Request body (partial/map) | Positional required | `Map<String, dynamic> fields` |

**Rationale**:
- Path parameters are always required → positional makes intent clear
- Query parameters are often optional → named parameters with `?`
- Cleaner HTTP requests (no null values sent)
- Reduces bandwidth
- Lets server use its defaults when parameters are omitted

**Pattern - Query Parameters**:
```dart
Future<List<User>> listUsers({
  int? page,
  int? limit,
  String? status,
}) async {
  final response = await _dio.get(
    '/users',
    queryParameters: {
      if (page != null) 'page': page,
      if (limit != null) 'limit': limit,
      if (status != null) 'status': status,
    },
  );
  // ...
}
```

**Pattern - Mixed Parameters**:
```dart
// Path param (positional) + Body param (positional)
Future<User> updateUser(String userId, User user) async { ... }

// Path param (positional) + Query params (named optional)
Future<List<Order>> getUserOrders(
  String userId, {
  int? page,
  String? status,
}) async { ... }
```

**Pattern - Header Parameters**:
```dart
Future<List<Product>> getProducts({
  String? acceptLanguage,
  String? xRequestId,
}) async {
  final response = await _dio.get(
    '/products',
    options: Options(
      headers: {
        if (acceptLanguage != null) 'Accept-Language': acceptLanguage,
        if (xRequestId != null) 'X-Request-ID': xRequestId,
      },
    ),
  );
  // ...
}
```

### 6. Return Types: Direct Types (No Wrapper)

**Decision**: Return `T` or `List<T>` directly, without pagination wrappers.

**Rationale**:
- Simpler API surface
- Matches most common use cases
- Pagination wrapper can be added as a future enhancement if needed

**Pattern**:
```dart
Future<User> getUser(String userId);
Future<List<User>> listUsers({int? page, int? limit});
```

### 7. API Client Configuration: Configuration Classes

**Decision**: Use separate configuration classes for each ACDC pillar, composed into a main `AcdcConfig` class.

**Rationale**:
- Type-safe: Each config class documents its options with types and defaults
- Discoverable: IDE autocomplete shows all available options
- Testable: Easy to create and share test configurations
- Serializable: Configs can be loaded from JSON/YAML if needed
- Optional features: `null` config = feature disabled
- Extensible: Add new config classes without breaking existing code
- Exposes full Dart-ACDC capabilities without limiting users

**Configuration Classes**:
```dart
import 'package:dart_acdc/dart_acdc.dart'; // Provides LogLevel, Logger, TokenProvider

class AcdcConfig {
  final String baseUrl;
  final AuthConfig? auth;
  final CacheConfig? cache;
  final LogConfig? log;
  final OfflineConfig? offline;
  final SecurityConfig? security;

  const AcdcConfig({
    required this.baseUrl,
    this.auth,
    this.cache,
    this.log,
    this.offline,
    this.security,
  });
}

class AuthConfig {
  final String tokenRefreshUrl;
  final String? clientId;
  final String? clientSecret;
  final Duration refreshThreshold;
  final bool useSecureStorage;
  final TokenProvider? customTokenProvider; // From dart_acdc

  const AuthConfig({
    required this.tokenRefreshUrl,
    this.clientId,
    this.clientSecret,
    this.refreshThreshold = const Duration(minutes: 5),
    this.useSecureStorage = true,
    this.customTokenProvider,
  });
}

class CacheConfig {
  final Duration ttl;
  final int maxDiskSizeBytes;
  final bool encrypt;
  final bool enableUserIsolation;

  const CacheConfig({
    this.ttl = const Duration(hours: 1),
    this.maxDiskSizeBytes = 20 * 1024 * 1024, // 20 MB
    this.encrypt = true,
    this.enableUserIsolation = true,
  });
}

class LogConfig {
  final LogLevel level;            // From dart_acdc: none, error, warning, info, debug, verbose
  final bool redactSensitiveData;
  final Logger? customLogger;      // From dart_acdc: for custom logging (e.g., Sentry)

  const LogConfig({
    this.level = LogLevel.info,
    this.redactSensitiveData = true,
    this.customLogger,
  });
}

class OfflineConfig {
  final bool enabled;
  final bool failFast;

  const OfflineConfig({
    this.enabled = true,
    this.failFast = false,
  });
}

class SecurityConfig {
  final List<String>? certificatePins;
  final bool reportOnlyMode;

  const SecurityConfig({
    this.certificatePins,
    this.reportOnlyMode = false,
  });
}
```

## Generated Code Examples

### Abstract Interface

```dart
import 'package:dart_acdc/dart_acdc.dart';
import '../models/health_status.dart';
import '../models/user.dart';

/// Interface for User remote operations.
///
/// Implement this for mocking in tests.
///
/// All methods may throw:
/// - [AcdcAuthException] - 401/403 errors, token refresh failures
/// - [AcdcNetworkException] - Offline, timeouts, DNS failures
/// - [AcdcServerException] - 5xx server errors
/// - [AcdcClientException] - 4xx client errors (400, 404, 422, etc.)
/// - [AcdcSecurityException] - Certificate pinning failures
abstract class UserRemoteDataSource {
  /// Get user by ID
  Future<User> getUser(String userId);

  /// Create a new user
  Future<User> createUser(User user);

  /// Update an existing user (full replacement)
  Future<User> updateUser(String userId, User user);

  /// Partially update a user
  Future<User> patchUser(String userId, Map<String, dynamic> fields);

  /// Delete a user
  Future<void> deleteUser(String userId);

  /// List users with pagination
  Future<List<User>> listUsers({
    int? page,
    int? limit,
  });

  /// Check API health (no parameters example)
  Future<HealthStatus> getHealthCheck();
}
```

### Implementation

```dart
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import '../models/health_status.dart';
import '../models/user.dart';
import 'user_remote_data_source.dart';

class UserRemoteDataSourceImpl implements UserRemoteDataSource {
  final Dio _dio;

  UserRemoteDataSourceImpl(this._dio);

  @override
  Future<User> getUser(String userId) async {
    try {
      final response = await _dio.get('/users/$userId');
      return User.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<User> createUser(User user) async {
    try {
      final response = await _dio.post(
        '/users',
        data: user.toJson(),
      );
      return User.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<User> updateUser(String userId, User user) async {
    try {
      final response = await _dio.put(
        '/users/$userId',
        data: user.toJson(),
      );
      return User.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<User> patchUser(String userId, Map<String, dynamic> fields) async {
    try {
      final response = await _dio.patch(
        '/users/$userId',
        data: fields,
      );
      return User.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<void> deleteUser(String userId) async {
    try {
      await _dio.delete('/users/$userId');
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<List<User>> listUsers({
    int? page,
    int? limit,
  }) async {
    try {
      final response = await _dio.get(
        '/users',
        queryParameters: {
          if (page != null) 'page': page,
          if (limit != null) 'limit': limit,
        },
      );
      return (response.data as List)
          .map((e) => User.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<HealthStatus> getHealthCheck() async {
    try {
      final response = await _dio.get('/health');
      return HealthStatus.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }
}
```

### Model Class (json_serializable)

```dart
import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable()
class User {
  /// Unique identifier
  final String id;

  /// User's email address
  final String email;

  /// Display name
  @JsonKey(name: 'display_name')
  final String? displayName;

  /// Account creation timestamp
  @JsonKey(name: 'created_at')
  final DateTime createdAt;

  const User({
    required this.id,
    required this.email,
    this.displayName,
    required this.createdAt,
  });

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);

  Map<String, dynamic> toJson() => _$UserToJson(this);
}
```

### API Client Factory

```dart
import 'package:dart_acdc/dart_acdc.dart';
import 'package:dio/dio.dart';
import 'config/config.dart';

class ApiClient {
  /// Creates a configured Dio instance with ACDC features.
  static Dio createDio(AcdcConfig config) {
    var builder = AcdcClientBuilder().withBaseUrl(config.baseUrl);

    if (config.auth != null) {
      builder = builder.withAuthentication(
        tokenRefreshUrl: config.auth!.tokenRefreshUrl,
        clientId: config.auth!.clientId,
        clientSecret: config.auth!.clientSecret,
        refreshThreshold: config.auth!.refreshThreshold,
        tokenProvider: config.auth!.customTokenProvider,
        useSecureStorage: config.auth!.useSecureStorage,
      );
    }

    if (config.cache != null) {
      builder = builder.withCache(
        ttl: config.cache!.ttl,
        maxDiskSize: config.cache!.maxDiskSizeBytes,
        encrypt: config.cache!.encrypt,
        userIsolation: config.cache!.enableUserIsolation,
      );
    }

    if (config.log != null) {
      builder = builder.withLogging(
        level: config.log!.level,
        redactSensitiveData: config.log!.redactSensitiveData,
        logger: config.log!.customLogger,
      );
    }

    if (config.offline?.enabled == true) {
      builder = builder.withOfflineDetection(
        failFast: config.offline!.failFast,
      );
    }

    if (config.security?.certificatePins != null) {
      builder = builder.withCertificatePinning(
        pins: config.security!.certificatePins!,
        reportOnly: config.security!.reportOnlyMode,
      );
    }

    return builder.build();
  }
}
```

## Generated Folder Structure

```
lib/
├── petstore_api.dart                        # Barrel export file (named after API)
├── api_client.dart                          # ApiClient.createDio() factory
├── config/
│   ├── config.dart                          # Barrel export for all configs
│   ├── acdc_config.dart                     # Main AcdcConfig class
│   ├── auth_config.dart                     # AuthConfig
│   ├── cache_config.dart                    # CacheConfig
│   ├── log_config.dart                      # LogConfig
│   ├── offline_config.dart                  # OfflineConfig
│   └── security_config.dart                 # SecurityConfig
├── models/
│   ├── models.dart                          # Barrel export for all models
│   ├── user.dart
│   ├── user.g.dart                          # generated by json_serializable
│   ├── product.dart
│   └── product.g.dart
└── remote_data_sources/
    ├── remote_data_sources.dart             # Barrel export for all data sources
    ├── user_remote_data_source.dart         # abstract interface
    ├── user_remote_data_source_impl.dart    # implementation
    ├── product_remote_data_source.dart
    └── product_remote_data_source_impl.dart
```

### Barrel Export Example

**`lib/petstore_api.dart`** (main entry point):
```dart
// Configuration
export 'config/config.dart';

// Models
export 'models/models.dart';

// Remote Data Sources
export 'remote_data_sources/remote_data_sources.dart';

// API Client
export 'api_client.dart';
```

This allows users to import everything with a single line:
```dart
import 'package:petstore_api/petstore_api.dart';
```

## Usage Examples

### Production Setup

```dart
// Minimal configuration (just base URL)
final dio = ApiClient.createDio(
  AcdcConfig(baseUrl: 'https://api.example.com'),
);

// Full configuration with all ACDC features
final dio = ApiClient.createDio(
  AcdcConfig(
    baseUrl: 'https://api.example.com',
    auth: AuthConfig(
      tokenRefreshUrl: 'https://api.example.com/auth/refresh',
      clientId: 'my-app',
      refreshThreshold: const Duration(minutes: 10),
    ),
    cache: CacheConfig(
      ttl: const Duration(hours: 2),
      maxDiskSizeBytes: 50 * 1024 * 1024, // 50 MB
      encrypt: true,
    ),
    log: LogConfig(
      level: LogLevel.debug,
      redactSensitiveData: true,
    ),
    offline: OfflineConfig(enabled: true),
    security: SecurityConfig(
      certificatePins: ['sha256/AAAA...', 'sha256/BBBB...'],
    ),
  ),
);

// Create data source instance
final UserRemoteDataSource userDataSource = UserRemoteDataSourceImpl(dio);

// Use the data source
final user = await userDataSource.getUser('123');
```

### Testing with Mocks

```dart
import 'package:dart_acdc/dart_acdc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:petstore_api/petstore_api.dart';

import 'user_remote_data_source_test.mocks.dart';

@GenerateMocks([UserRemoteDataSource])
void main() {
  late MockUserRemoteDataSource mockUserDataSource;

  setUp(() {
    mockUserDataSource = MockUserRemoteDataSource();
  });

  group('getUser', () {
    test('should return user when call succeeds', () async {
      // Arrange
      final expectedUser = User(
        id: '123',
        email: 'test@example.com',
        createdAt: DateTime.now(),
      );
      when(mockUserDataSource.getUser('123'))
          .thenAnswer((_) async => expectedUser);

      // Act
      final result = await mockUserDataSource.getUser('123');

      // Assert
      expect(result, expectedUser);
      verify(mockUserDataSource.getUser('123')).called(1);
    });

    test('should throw AcdcNetworkException when offline', () async {
      // Arrange
      when(mockUserDataSource.getUser(any))
          .thenThrow(AcdcNetworkException('No internet connection'));

      // Act & Assert
      expect(
        () => mockUserDataSource.getUser('123'),
        throwsA(isA<AcdcNetworkException>()),
      );
    });

    test('should throw AcdcAuthException when unauthorized', () async {
      // Arrange
      when(mockUserDataSource.getUser(any))
          .thenThrow(AcdcAuthException('Token expired'));

      // Act & Assert
      expect(
        () => mockUserDataSource.getUser('123'),
        throwsA(isA<AcdcAuthException>()),
      );
    });
  });

  group('deleteUser', () {
    test('should complete without error when delete succeeds', () async {
      // Arrange
      when(mockUserDataSource.deleteUser('123'))
          .thenAnswer((_) async {});

      // Act & Assert
      await expectLater(
        mockUserDataSource.deleteUser('123'),
        completes,
      );
      verify(mockUserDataSource.deleteUser('123')).called(1);
    });
  });
}
```

## Consequences

### Positive

- Clear separation between interface and implementation
- Easy to mock for testing
- Explicit error handling with typed exceptions
- Follows established Flutter/Dart conventions
- Clean, readable generated code
- Configuration classes expose full Dart-ACDC capabilities
- Type-safe configuration with IDE autocomplete
- Configurations are reusable and can be serialized

### Negative

- Two files per resource (interface + implementation) increases file count
- Slightly more verbose than single-class approach
- Multiple config files add to generated file count

### Neutral

- Requires users to understand the interface/implementation pattern
- Generated code follows clean architecture conventions (may differ from simpler projects)
- Users need to import config classes when customizing beyond defaults

## Future Considerations

The following items are intentionally deferred for future iterations:

### Pagination Wrapper
Currently, list methods return `List<T>` directly. A future enhancement could add:
```dart
class PagedResponse<T> {
  final List<T> items;
  final int? totalCount;
  final int? nextPage;
  final bool hasMore;
}
```

### File Upload / Multipart
File upload endpoints (`multipart/form-data`) require special handling:
```dart
Future<UploadResult> uploadAvatar(String userId, File file) async {
  final formData = FormData.fromMap({
    'file': await MultipartFile.fromFile(file.path),
  });
  final response = await _dio.post('/users/$userId/avatar', data: formData);
  // ...
}
```
This will be addressed when implementing the generator templates.

### Streaming Responses
Support for Server-Sent Events (SSE) or streaming responses is not covered.

### WebSocket Support
Real-time WebSocket connections are outside the scope of this generator.

### Freezed Models
An alternative to `json_serializable` using Freezed for immutable models with `copyWith`:
```dart
@freezed
class User with _$User {
  const factory User({
    required String id,
    required String email,
    String? displayName,
  }) = _User;

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
}
```

### Custom Serialization
Support for custom date formats, enum serialization strategies, and other serialization edge cases.

## References

- [Dart-ACDC Library Documentation](./dart-acdc-library.md)
- [Project Vision](./project-vision.md)
- [OpenAPI Generator Architecture](./creating-generators.md)
