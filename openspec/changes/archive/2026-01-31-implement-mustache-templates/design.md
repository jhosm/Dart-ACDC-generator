# Design: Implement Mustache Templates

## Context

Mustache templates are the core of OpenAPI Generator. They define the exact code that will be generated. Our templates must produce code that:
1. Follows ADR-001 architectural decisions
2. Integrates with Dart-ACDC library
3. Follows Flutter/Dart conventions
4. Is production-ready

## Goals

- Generate clean, readable Dart code
- Follow `*RemoteDataSource` naming convention
- Implement interface/implementation split for testability
- Use `json_serializable` for model serialization
- Include comprehensive documentation

## Non-Goals

- Freezed model support (future enhancement)
- Pagination wrapper types (future enhancement)
- File upload/multipart support (future enhancement)
- Streaming/WebSocket support (out of scope)

## Decisions

### Decision 1: Two-File API Pattern

**What**: Generate both `*_remote_data_source.dart` (interface) and `*_remote_data_source_impl.dart` (implementation) for each API.

**Why**:
- Enables easy mocking without Dio/network dependencies
- Follows dependency inversion principle
- Allows swapping implementations (e.g., mock vs production)
- ADR-001 specifies this pattern

**Example**:
```
lib/remote_data_sources/
├── user_remote_data_source.dart        # abstract class
└── user_remote_data_source_impl.dart   # implementation
```

### Decision 2: Parameter Conventions

**What**: Use type-based parameter conventions as specified in ADR-001.

| OpenAPI Location | Dart Convention |
|------------------|-----------------|
| Path parameters | Positional required |
| Query parameters (optional) | Named optional `{int? page}` |
| Query parameters (required) | Named required `{required String filter}` |
| Request body | Positional required |

**Why**:
- Path parameters are always required → positional makes sense
- Query parameters are often optional → named parameters with `?`
- Matches Dart idioms and provides good IDE experience

### Decision 3: Error Handling Pattern

**What**: Every method wraps `DioException` → `AcdcException`.

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

**Why**:
- Consistent, typed error handling across all API calls
- Hides Dio implementation details
- ADR-001 specifies this pattern

### Decision 4: JSON Serialization with json_serializable

**What**: Use `@JsonSerializable()` and `@JsonKey()` annotations.

**Why**:
- Standard in Flutter ecosystem
- Good IDE support
- Type-safe
- Handles complex cases (custom names, enum serialization)

**Alternatives considered**:
- Freezed: Rejected for initial version - adds complexity
- Manual fromJson/toJson: Rejected - too verbose, error-prone
- Built_value: Rejected - less common, more boilerplate

### Decision 5: Null Filtering for Query Parameters

**What**: Only include non-null query parameters in requests.

```dart
queryParameters: {
  if (page != null) 'page': page,
  if (limit != null) 'limit': limit,
},
```

**Why**:
- Cleaner HTTP requests (no null values sent)
- Reduces bandwidth
- Lets server use its defaults

### Decision 6: Configuration Class Templates

**What**: Generate separate config classes for each ACDC pillar.

```
lib/config/
├── config.dart          # barrel export
├── acdc_config.dart     # main config
├── auth_config.dart     # authentication
├── cache_config.dart    # caching
├── log_config.dart      # logging
├── offline_config.dart  # offline support
└── security_config.dart # certificate pinning
```

**Why**:
- Type-safe configuration
- IDE autocomplete shows all options
- Optional features: `null` config = disabled
- Matches ADR-001 specification

## Template Variables Reference

Key Mustache variables we'll use:

### Package Level
- `{{pubName}}` - Package name (e.g., `petstore_api`)
- `{{pubVersion}}` - Version
- `{{pubDescription}}` - Description

### API Level
- `{{classname}}` - API class name (e.g., `User`)
- `{{operations}}` - List of operations
- `{{imports}}` - Required imports

### Operation Level
- `{{operationId}}` - Method name
- `{{httpMethod}}` - HTTP method
- `{{path}}` - Endpoint path
- `{{summary}}` - Short description
- `{{returnType}}` - Return type
- `{{allParams}}`, `{{pathParams}}`, `{{queryParams}}`, `{{bodyParam}}`

### Model Level
- `{{classname}}` - Model class name
- `{{vars}}` - List of properties
- `{{description}}` - Model description

### Property Level
- `{{name}}` - Property variable name
- `{{baseName}}` - Property name in JSON
- `{{dataType}}` - Dart type
- `{{required}}` - Is required

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Template complexity | Start simple, iterate |
| Edge cases in OpenAPI specs | Test with multiple real-world specs |
| Generated code verbosity | Provide barrel exports, document usage |

## Open Questions

1. **Should we generate `.g.dart` files or require build_runner?**
   - Current decision: Require build_runner (standard practice)
   - Benefit: Users control regeneration
   - Trade-off: Extra step for users

2. **Should we support discriminator/oneOf patterns?**
   - Current decision: Defer to future enhancement
   - Reason: Complex, not needed for MVP
