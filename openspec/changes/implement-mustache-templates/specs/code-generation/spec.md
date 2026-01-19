# Capability: Code Generation

Templates and code generation logic for producing Dart API clients from OpenAPI specifications.

## ADDED Requirements

### Requirement: Model Class Generation

The generator SHALL produce Dart model classes for each schema defined in the OpenAPI specification.

#### Scenario: Basic model with required properties
- **WHEN** an OpenAPI schema defines a `User` object with required properties `id` (string) and `email` (string)
- **THEN** the generated `User` class SHALL have `final String id` and `final String email` as required constructor parameters
- **AND** the class SHALL have `@JsonSerializable()` annotation
- **AND** the class SHALL have a `factory User.fromJson(Map<String, dynamic> json)` method
- **AND** the class SHALL have a `Map<String, dynamic> toJson()` method

#### Scenario: Model with optional properties
- **WHEN** an OpenAPI schema defines an optional property `displayName` (string)
- **THEN** the generated property SHALL be nullable: `final String? displayName`
- **AND** it SHALL NOT be required in the constructor

#### Scenario: Model with JSON key mapping
- **WHEN** an OpenAPI schema defines a property with name `created_at`
- **THEN** the generated property SHALL use camelCase: `final DateTime createdAt`
- **AND** the property SHALL have `@JsonKey(name: 'created_at')` annotation

#### Scenario: Model with documentation
- **WHEN** an OpenAPI schema or property has a `description` field
- **THEN** the generated code SHALL include a documentation comment with that description

### Requirement: Remote Data Source Interface Generation

The generator SHALL produce abstract interface classes for each API tag/group.

#### Scenario: Interface naming convention
- **WHEN** an API is generated for the `User` tag
- **THEN** the interface SHALL be named `UserRemoteDataSource`
- **AND** it SHALL be in a file named `user_remote_data_source.dart`

#### Scenario: Interface method signatures
- **WHEN** an OpenAPI path defines a GET operation with operationId `getUser`
- **THEN** the interface SHALL have an abstract method `Future<User> getUser(...)`

#### Scenario: Interface documentation
- **WHEN** a remote data source interface is generated
- **THEN** the class documentation SHALL list all possible exceptions that methods may throw
- **AND** the documentation SHALL include `AcdcAuthException`, `AcdcNetworkException`, `AcdcServerException`, `AcdcClientException`, and `AcdcSecurityException`

### Requirement: Remote Data Source Implementation Generation

The generator SHALL produce concrete implementation classes that implement the interfaces.

#### Scenario: Implementation naming convention
- **WHEN** an implementation is generated for `UserRemoteDataSource`
- **THEN** the class SHALL be named `UserRemoteDataSourceImpl`
- **AND** it SHALL be in a file named `user_remote_data_source_impl.dart`
- **AND** it SHALL implement `UserRemoteDataSource`

#### Scenario: Dio constructor injection
- **WHEN** an implementation class is generated
- **THEN** it SHALL accept a `Dio` instance via constructor: `UserRemoteDataSourceImpl(this._dio)`
- **AND** the Dio instance SHALL be stored as a private final field

#### Scenario: Error handling in methods
- **WHEN** an implementation method is generated
- **THEN** the method body SHALL wrap the Dio call in `try/catch`
- **AND** `DioException` SHALL be caught and converted using `throw AcdcException.fromDioException(e)`

### Requirement: Parameter Handling

The generator SHALL handle different parameter types according to established conventions.

#### Scenario: Path parameter handling
- **WHEN** an OpenAPI operation defines a path parameter `userId`
- **THEN** it SHALL be a positional required parameter in the method signature
- **AND** the implementation SHALL interpolate it in the URL path

#### Scenario: Optional query parameter handling
- **WHEN** an OpenAPI operation defines an optional query parameter `page`
- **THEN** it SHALL be a named optional parameter: `{int? page}`
- **AND** the implementation SHALL only include it in the request if non-null

#### Scenario: Required query parameter handling
- **WHEN** an OpenAPI operation defines a required query parameter `filter`
- **THEN** it SHALL be a named required parameter: `{required String filter}`

#### Scenario: Request body handling
- **WHEN** an OpenAPI operation defines a request body with a model type `User`
- **THEN** it SHALL be a positional required parameter: `User user`
- **AND** the implementation SHALL call `user.toJson()` for the request data

#### Scenario: Header parameter handling
- **WHEN** an OpenAPI operation defines a header parameter `Accept-Language`
- **THEN** it SHALL be a named parameter (optional or required based on schema)
- **AND** the implementation SHALL include it in Dio's `Options.headers`

### Requirement: Response Handling

The generator SHALL handle different response types correctly.

#### Scenario: Single object response
- **WHEN** an OpenAPI operation returns a single `User` object
- **THEN** the method return type SHALL be `Future<User>`
- **AND** the implementation SHALL deserialize using `User.fromJson(response.data)`

#### Scenario: Array response
- **WHEN** an OpenAPI operation returns an array of `User` objects
- **THEN** the method return type SHALL be `Future<List<User>>`
- **AND** the implementation SHALL map and deserialize each item

#### Scenario: Void response
- **WHEN** an OpenAPI operation returns no content (204) or success with no body
- **THEN** the method return type SHALL be `Future<void>`
- **AND** the implementation SHALL not attempt to deserialize the response

### Requirement: API Client Factory

The generator SHALL produce an `ApiClient` class with a `createDio` factory method.

#### Scenario: Basic Dio creation
- **WHEN** `ApiClient.createDio(config)` is called with an `AcdcConfig`
- **THEN** it SHALL return a configured `Dio` instance
- **AND** the Dio instance SHALL have the base URL from the config

#### Scenario: Conditional feature setup
- **WHEN** `AcdcConfig.auth` is non-null
- **THEN** authentication SHALL be configured on the Dio instance
- **WHEN** `AcdcConfig.auth` is null
- **THEN** authentication SHALL NOT be configured

### Requirement: Configuration Classes

The generator SHALL produce typed configuration classes for each ACDC pillar.

#### Scenario: Main config class structure
- **WHEN** configuration classes are generated
- **THEN** `AcdcConfig` SHALL have a required `baseUrl` property
- **AND** it SHALL have optional `auth`, `cache`, `log`, `offline`, and `security` properties

#### Scenario: Auth config options
- **WHEN** `AuthConfig` is generated
- **THEN** it SHALL have `tokenRefreshUrl`, `clientId`, `clientSecret`, `refreshThreshold`, `useSecureStorage`, and `customTokenProvider` properties

### Requirement: Barrel Export Files

The generator SHALL produce barrel export files for convenient imports.

#### Scenario: Main barrel export
- **WHEN** code is generated for a package named `petstore_api`
- **THEN** a file `lib/petstore_api.dart` SHALL be created
- **AND** it SHALL export all models, remote data sources, config, and api_client

#### Scenario: Models barrel export
- **WHEN** models are generated
- **THEN** a file `lib/models/models.dart` SHALL be created
- **AND** it SHALL export all model files

### Requirement: pubspec.yaml Generation

The generator SHALL produce a valid pubspec.yaml with all required dependencies.

#### Scenario: Core dependencies included
- **WHEN** pubspec.yaml is generated
- **THEN** it SHALL include dependencies: `dio`, `dart_acdc`, `json_annotation`
- **AND** it SHALL include dev_dependencies: `build_runner`, `json_serializable`

#### Scenario: Package metadata from config
- **WHEN** pubspec.yaml is generated with `pubName: my_api`
- **THEN** the `name` field SHALL be `my_api`
